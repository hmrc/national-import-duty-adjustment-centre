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

import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{BankDetails, CreateClaimRequest, UploadedFile}

trait TestData {

  val claimRequest = CreateClaimRequest(
    userId = "some-id",
    claimType = "some-claim-type",
    uploads = uploadedFiles("reference"),
    reclaimDutyTypes = Set("01"),
    bankDetails = Some(BankDetails("account name", "001122", "12345678"))
  )

  val content = EISCreateCaseRequest.Content(
    ClaimType = "some-claim-type",
    DutyDetails = Seq(DutyDetail("01", "0", "0")),
    PaymentDetails = Some(PaymentDetails("account name", "12345678", "001122"))
  )

  def eisCreateCaseRequest(createClaimRequest: CreateClaimRequest): EISCreateCaseRequest = new EISCreateCaseRequest(
    AcknowledgementReference = "",
    ApplicationType = "",
    OriginatingSystem = "",
    Content = EISCreateCaseRequest.Content(createClaimRequest)
  )

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

  val eisSuccessResponse: EISCreateCaseSuccess =
    EISCreateCaseSuccess("case-id", "processing-date", "status", "status-text")

  val eisFailResponse: EISCreateCaseError =
    EISCreateCaseError("timestamp", "correlationId", "errorCode", "errorMessage")

}
