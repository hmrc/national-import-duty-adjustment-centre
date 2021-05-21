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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis

import java.time.Instant
import play.api.libs.json._
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

sealed trait EISUpdateCaseResponse

case class EISUpdateCaseSuccess(CaseID: String, ProcessingDate: Instant, Status: String, StatusText: String)
    extends EISUpdateCaseResponse

object EISUpdateCaseSuccess {

  implicit val formats: Format[EISUpdateCaseSuccess] =
    Json.format[EISUpdateCaseSuccess]

}

case class EISUpdateCaseError(errorDetail: EISErrorDetail) extends EISUpdateCaseResponse

object EISUpdateCaseError {

  def fromStatusAndMessage(status: Int, message: String): EISUpdateCaseError =
    EISUpdateCaseError(EISErrorDetail(Some(s"STATUS${status.toString}"), Some(message)))

  implicit val formats: Format[EISUpdateCaseError] =
    Json.format[EISUpdateCaseError]

}

object EISUpdateCaseResponse {

  implicit def reads: Reads[EISUpdateCaseResponse] =
    Reads {
      case jsObject: JsObject if (jsObject \ "CaseID").isDefined =>
        EISUpdateCaseSuccess.formats.reads(jsObject)
      case jsValue =>
        EISUpdateCaseError.formats.reads(jsValue)
    }

  implicit def writes: Writes[EISUpdateCaseResponse] = {
    case s: EISUpdateCaseSuccess =>
      EISUpdateCaseSuccess.formats.writes(s)
    case e: EISUpdateCaseError =>
      EISUpdateCaseError.formats.writes(e)
  }

  final def shouldRetry(response: Try[EISUpdateCaseResponse]): Boolean = {
    response match {
      case Failure(e: UpstreamErrorResponse) if e.statusCode == 429 => true
      case _ => false
    }
  }

  final def errorMessage(response: Try[EISUpdateCaseResponse]): String = {
    response match {
      case Failure(e: UpstreamErrorResponse) if e.statusCode == 429 => "Quota reached"
    }
  }

  final def delayInterval(response: Try[EISUpdateCaseResponse]): Option[FiniteDuration] = {
    response match {
      case Failure(e: UpstreamErrorResponse) if e.statusCode == 429 =>

        try {
          Some(FiniteDuration(e.getMessage().toLong, TimeUnit.MILLISECONDS))
        } catch {
          case e: NumberFormatException => None
        }
    }
  }

}
