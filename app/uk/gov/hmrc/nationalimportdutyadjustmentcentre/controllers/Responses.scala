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

import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, Forbidden, Unauthorized}

object Responses {

  val missingXCorrelationIdResponse: Result = BadRequest(
    Json.obj(
      "statusCode" -> JsNumber(BadRequest.header.status),
      "message"    -> JsString("Missing header x-correlation-id")
    )
  )

  val invalidEORINumberResponse: Result = Unauthorized(
    Json.obj("statusCode" -> JsNumber(Unauthorized.header.status), "message" -> JsString("Invalid user"))
  )

  def forbiddenResponse(message: String): Result = Forbidden(
    Json.obj("statusCode" -> JsNumber(Forbidden.header.status), "message" -> JsString(message))
  )

}
