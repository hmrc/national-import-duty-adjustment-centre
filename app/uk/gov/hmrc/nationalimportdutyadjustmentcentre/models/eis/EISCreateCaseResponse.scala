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

sealed trait EISCreateCaseResponse

case class EISCreateCaseSuccess(CaseID: String, ProcessingDate: Instant, Status: String, StatusText: String)
    extends EISCreateCaseResponse

object EISCreateCaseSuccess {

  implicit val formats: OFormat[EISCreateCaseSuccess] = Json.format[EISCreateCaseSuccess]

}

case class EISCreateCaseError(errorDetail: EISErrorDetail) extends EISCreateCaseResponse

object EISCreateCaseError {

  def fromStatusAndMessage(status: Int, message: String): EISCreateCaseError =
    EISCreateCaseError(EISErrorDetail(Some(s"STATUS${status.toString}"), Some(message)))

  implicit val formats: OFormat[EISCreateCaseError] = Json.format[EISCreateCaseError]

}

object EISCreateCaseResponse {

  implicit def reads: Reads[EISCreateCaseResponse] =
    Reads {
      case jsObject: JsObject if (jsObject \ "CaseID").isDefined =>
        EISCreateCaseSuccess.formats.reads(jsObject)
      case jsValue =>
        EISCreateCaseError.formats.reads(jsValue)
    }

  implicit def writes: Writes[EISCreateCaseResponse] = {
    case s: EISCreateCaseSuccess =>
      EISCreateCaseSuccess.formats.writes(s)
    case e: EISCreateCaseError =>
      EISCreateCaseError.formats.writes(e)
  }

}
