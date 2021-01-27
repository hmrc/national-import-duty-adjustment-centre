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

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.MicroserviceAuthConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{
  ApiError,
  EISCreateCaseError,
  EISCreateCaseRequest,
  EISCreateCaseSuccess
}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{
  CreateClaimRequest,
  CreateClaimResponse,
  CreateClaimResult
}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.services.{ClaimService, FileTransferService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ClaimController @Inject() (
  val authConnector: MicroserviceAuthConnector,
  cc: ControllerComponents,
  claimService: ClaimService,
  fileTransferService: FileTransferService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with AuthActions with WithCorrelationId {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withAuthorised {
      withCorrelationId { correlationId: String =>
        withJsonBody[CreateClaimRequest] { createClaimRequest: CreateClaimRequest =>
          val eisCreateCaseRequest = EISCreateCaseRequest(
            AcknowledgementReference = correlationId.replace("-", ""),
            ApplicationType = "NIDAC",
            OriginatingSystem = "Digital",
            Content = EISCreateCaseRequest.Content(createClaimRequest)
          )

          claimService.createClaim(eisCreateCaseRequest, correlationId) flatMap {
            case success: EISCreateCaseSuccess =>
              fileTransferService.transferFiles(success.CaseID, correlationId, createClaimRequest.uploads) map {
                uploadResults =>
                  Created(
                    Json.toJson(
                      CreateClaimResponse(
                        correlationId = correlationId,
                        result = Some(CreateClaimResult(success.CaseID, uploadResults))
                      )
                    )
                  )

              }

            case error: EISCreateCaseError =>
              Future(
                BadRequest(
                  Json.toJson(
                    CreateClaimResponse(
                      correlationId = correlationId,
                      error = Some(
                        ApiError(
                          errorCode = error.errorCode.getOrElse("ERROR_UPSTREAM_UNDEFINED"),
                          errorMessage = error.errorMessage
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }
  }

}
