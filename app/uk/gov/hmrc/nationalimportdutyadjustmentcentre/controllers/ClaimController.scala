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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.{CreateCaseConnector, MicroserviceAuthConnector}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{ApiError, EISCreateCaseError, EISCreateCaseSuccess}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{
  CreateClaimResponse,
  CreateClaimResult,
  CreateEISClaimRequest
}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.services.FileTransferService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ClaimController @Inject() (
  val authConnector: MicroserviceAuthConnector,
  cc: ControllerComponents,
  createCaseConnector: CreateCaseConnector,
  fileTransferService: FileTransferService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with AuthActions with WithCorrelationId {

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withAuthorised {
      withCorrelationId { correlationId: String =>
        withJsonBody[CreateEISClaimRequest] { createClaimRequest: CreateEISClaimRequest =>
          createCaseConnector.submitClaim(createClaimRequest.eisRequest, correlationId) flatMap {
            case success: EISCreateCaseSuccess =>
              fileTransferService.transferFiles(success.CaseID, correlationId, createClaimRequest.uploadedFiles) map {
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
                      error = Some(ApiError(errorCode = error.ErrorCode, errorMessage = Some(error.ErrorMessage)))
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
