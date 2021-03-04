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

import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class UpdateSuccessResponse(
  CaseID: String,
  ProcessingDate: ZonedDateTime = ZonedDateTime.now(),
  Status: String = "OK",
  StatusText: String = "Case updated"
)

object UpdateSuccessResponse {

  implicit val format: Format[UpdateSuccessResponse] = {
    implicit val zonedDateTimeFormat: Format[ZonedDateTime] = Format(
      Reads(
        json =>
          json.validate[String].flatMap { dateTime =>
            JsSuccess(ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME))
          }
      ),
      Writes(dateTime => JsString(DateTimeFormatter.ISO_INSTANT.format(dateTime)))
    )
    Json.format
  }

}

@Singleton()
class EISUpdateCaseController @Inject() (cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def post(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val caseId: String =
      (request.body \ "Content" \ "CaseID").toOption.map(_.as[String]).getOrElse("Missing Case ID In Request")
    Future(Ok(Json.toJson(UpdateSuccessResponse(CaseID = caseId))))
  }

}
