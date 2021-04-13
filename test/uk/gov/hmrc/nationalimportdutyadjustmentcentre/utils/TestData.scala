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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.utils

import java.time.{Instant, LocalDateTime, ZoneOffset}

import play.api.libs.json.JsString
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis._

trait TestData {

  val createClaimRequest: CreateEISClaimRequest =
    CreateEISClaimRequest(eisRequest = JsString("payload"), uploadedFiles = uploadedFiles("reference"))

  val updateClaimRequest: UpdateEISClaimRequest =
    UpdateEISClaimRequest(eisRequest = JsString("payload"), uploadedFiles = uploadedFiles("reference"))

  def uploadedFiles(upscanReferences: String*): Seq[UploadedFile] = upscanReferences.map(
    upscanReference =>
      UploadedFile(
        upscanReference = upscanReference,
        downloadUrl = s"downloadURL$upscanReference",
        checksum = s"checksum$upscanReference",
        fileName = s"fileName$upscanReference",
        fileMimeType = s"mimeType$upscanReference"
      )
  )

  val processingDate: Instant = LocalDateTime.of(2021, 4, 2, 9, 21).toInstant(ZoneOffset.UTC);

  val eisCreateSuccessResponse: EISCreateCaseSuccess =
    EISCreateCaseSuccess("case-id", processingDate, "status", "status-text")

  val eisCreateFailResponse: EISCreateCaseError =
    EISCreateCaseError("errorCode", "errorMessage", Some("correlationId"), Some(processingDate))

  val eisUpdateSuccessResponse: EISUpdateCaseSuccess =
    EISUpdateCaseSuccess("case-id", processingDate, "status", "status-text")

  val eisUpdateFailResponse: EISUpdateCaseError =
    EISUpdateCaseError("errorCode", "errorMessage", Some("correlationId"), Some(processingDate))

}
