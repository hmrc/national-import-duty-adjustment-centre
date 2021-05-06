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
import play.api.mvc.{Request, Result, _}
import uk.gov.hmrc.auth.core.AuthorisedFunctions
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.config.AppConfig

import scala.concurrent.{ExecutionContext, Future}

trait WithEORINumber extends AuthorisedFunctions { self: Results =>

  private val eoriIdentifier = "EORINumber"

  private val invalidEORINumberResponse = Unauthorized(
    Json.obj("statusCode" -> JsNumber(Unauthorized.header.status), "message" -> JsString("Invalid user"))
  )

  protected def withEORINumber(
    f: String => Future[Result]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[_], appConfig: AppConfig): Future[Result] =
    authorised().retrieve(allEnrolments) { enrolments =>
      val eoriFromEnrolments: Option[String] =
        enrolments
          .enrolments.find(en => appConfig.eoriEnrolments.contains(en.key))
          .flatMap(_.getIdentifier(eoriIdentifier)).map(_.value)

      eoriFromEnrolments match {
        case Some(eori) if appConfig.allowEori(eori) => f(eori)
        case _                                       => Future.successful(invalidEORINumberResponse)
      }
    }

}
