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

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.config.AppConfig
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.Future
import scala.util.Random

case class SuccessResponse(
                            CaseID: String,
                            ProcessingDate: ZonedDateTime = ZonedDateTime.now(),
                            Status: String = "OK",
                            StatusText: String = "Case created"
                          )

object SuccessResponse {

  implicit val format: Format[SuccessResponse] = {
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
class EISCreateCaseController @Inject() (servicesConfig: ServicesConfig, cc: ControllerComponents) extends BackendController(cc) {

  private val okResponse = Ok(
    Json.toJson(
      SuccessResponse(CaseID = servicesConfig.getConfString("eis.createcaseapi.stub.ref", "DEFAULT_PEGA_REF"))
    )
  )

  def post(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    Future.successful(okResponse)
  }

}
