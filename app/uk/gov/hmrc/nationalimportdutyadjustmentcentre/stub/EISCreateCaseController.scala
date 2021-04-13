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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.stub

import java.time.Instant
import java.time.format.DateTimeFormatter

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.config.AppConfig
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

case class CreateSuccessResponse(
  CaseID: String,
  ProcessingDate: Instant = Instant.now(),
  Status: String = "OK",
  StatusText: String = "Case created"
)

object CreateSuccessResponse {

  implicit val format: Format[CreateSuccessResponse] = {
    implicit val instantFormat: Format[Instant] = Format(
      Reads(
        json =>
          json.validate[String].flatMap { dateTime =>
            JsSuccess(Instant.parse(dateTime))
          }
      ),
      Writes(dateTime => JsString(DateTimeFormatter.ISO_INSTANT.format(dateTime)))
    )
    Json.format
  }

}

@Singleton()
class EISCreateCaseController @Inject() (appConfig: AppConfig, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def post(): Action[JsValue] = Action.async(parse.json) { _ =>
    Future(Ok(Json.toJson(CreateSuccessResponse(CaseID = appConfig.stubPegaCaseRef()))))
  }

}
