/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.controllers

import akka.actor.{ActorRef, ActorSystem, Props}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.{ForbiddenException, HeaderCarrier}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.actors.{FileTransferActor, FileTransferAuditActor}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.config.AppConfig
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.{FileTransferConnector, MicroserviceAuthConnector}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.controllers.Responses.forbiddenResponse
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{ApiError, EISCreateCaseError, EISCreateCaseSuccess}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{UploadedFile, _}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.services.CreateCaseService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class CreateClaimController @Inject() (
  val authConnector: MicroserviceAuthConnector,
  cc: ControllerComponents,
  createCaseService: CreateCaseService,
  fileTransferConnector: FileTransferConnector,
  auditConnector: AuditConnector,
  actorSystem: ActorSystem
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends BackendController(cc) with AuthActions with WithEORINumber with WithCorrelationId {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withAuthorised {
      withEORINumber { eoriNumber =>
        withCorrelationId { correlationId: String =>
          withJsonBody[CreateEISClaimRequest] { createClaimRequest: CreateEISClaimRequest =>
            createCaseService.submitClaim(eoriNumber, createClaimRequest.eisRequest, correlationId) flatMap {
              eisResponse =>
                val createClaimResponse: Future[CreateClaimResponse] = eisResponse match {
                  case success: EISCreateCaseSuccess =>
                    transferFilesToPega(success.CaseID, correlationId, createClaimRequest.uploadedFiles) map {
                      uploadResults =>
                        CreateClaimResponse(
                          correlationId = correlationId,
                          processingDate = Some(success.ProcessingDate),
                          result = Some(CreateClaimResult(success.CaseID, uploadResults))
                        )
                    }
                  case error: EISCreateCaseError =>
                    Future(
                      CreateClaimResponse(
                        correlationId = correlationId,
                        processingDate = Some(error.errorDetail.timestamp),
                        error = Some(
                          ApiError(
                            errorCode = error.errorDetail.errorCode,
                            errorMessage = error.errorDetail.errorMessage
                          )
                        )
                      )
                    )
                }

                createClaimResponse map { response =>
                  Ok(Json.toJson(response))
                }

            } recover {
              case fe: ForbiddenException => forbiddenResponse(fe.message)
            }
          }
        }
      }
    }
  }

  private def transferFilesToPega(
    caseReferenceNumber: String,
    conversationId: String,
    uploadedFiles: Seq[UploadedFile]
  )(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Seq[FileTransferResult]] = {

    val auditActor: ActorRef = actorSystem.actorOf(
      Props(classOf[FileTransferAuditActor], caseReferenceNumber, auditConnector, conversationId, hc, executionContext)
    )

    // Single-use actor responsible for transferring files batch to PEGA
    val fileTransferActor: ActorRef =
      actorSystem.actorOf(
        Props(classOf[FileTransferActor], caseReferenceNumber, fileTransferConnector, conversationId, auditActor)
      )

    fileTransferActor ! FileTransferActor.TransferMultipleFiles(uploadedFiles.zipWithIndex, uploadedFiles.size, hc)
    Future.successful(Seq.empty)
  }

}
