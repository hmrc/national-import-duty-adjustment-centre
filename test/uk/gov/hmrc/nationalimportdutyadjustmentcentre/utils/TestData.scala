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

import java.time.LocalDate

import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis._

trait TestData {

  val entryDate: LocalDate = LocalDate.of(2020, 12, 31)

  val claimRequest: CreateClaimRequest = CreateClaimRequest(
    userId = "some-id",
    contactDetails = ContactDetails("Adam", "Smith", "adam@smith.com", "01234567890"),
    importerAddress = UkAddress("Import Co Ltd", "Address Line 1", Some("Address Line 2"), "City", "PO12CD"),
    claimType = "some-claim-type",
    uploads = uploadedFiles("reference"),
    reclaimDutyTypes = Set("01"),
    bankDetails = Some(BankDetails("account name", "001122", "12345678")),
    entryDetails = EntryDetails("012", "123456Q", entryDate)
  )

  val content = EISCreateCaseRequest.Content(
    ClaimType = "some-claim-type",
    ImporterDetails = ImporterDetails(
      "Import Co Ltd",
      Address("Address Line 1", Some("Address Line 2"), "City", "PO12CD", "GB", "01234567890", "adam@smith.com")
    ),
    EntryProcessingUnit = "012",
    EntryNumber = "123456Q",
    EntryDate = "20201231",
    DutyDetails = Seq(DutyDetail("01", "0", "0")),
    PaymentDetails = Some(PaymentDetails("account name", "12345678", "001122")),
    FirstName = "Adam",
    LastName = "Smith"
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
