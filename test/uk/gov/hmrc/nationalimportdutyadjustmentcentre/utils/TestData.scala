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

import play.api.libs.json.{JsString, JsValue, Json}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis._

trait TestData {

  val validEORI = "GB1234567890"

  def validEnrolments(eoriNumber: String = validEORI): Enrolments = Enrolments(
    Set(Enrolment("HMRC-CTS-ORG", List(EnrolmentIdentifier("EORINumber", eoriNumber)), "Activated", None))
  )

  val createClaimRequest: CreateEISClaimRequest =
    CreateEISClaimRequest(eisRequest = JsString("payload"), uploadedFiles = uploadedFiles("reference"))

  val importerEORI                                              = "GB98574673905738"
  val agentEORI                                                 = "GB68469378594623"
  def createClaimImporterPayload(importerEori: String): JsValue = Json.parse(s"""
           |{
           |  "AcknowledgementReference": "39d5765e8f904a1e9281bd57ff86a79f",
           |  "ApplicationType": "NIDAC",
           |  "OriginatingSystem": "Digital",
           |  "Content": {
           |    "RepresentationType": "Importer",
           |    "ClaimType": "Account Sales",
           |    "ImporterDetails": {
           |      "EORI": "$importerEori",
           |      "Name": "ACME Importers Ltd",
           |      "Address": {
           |        "AddressLine1": "Unit 42",
           |        "AddressLine2": "West Industrial Estate",
           |        "City": "Walsall",
           |        "PostalCode": "WS1 2AB",
           |        "CountryCode": "GB",
           |        "TelephoneNumber": "01234567890",
           |        "EmailAddress": "tim@testing.com"
           |      }
           |    },
           |    "EntryProcessingUnit": "123",
           |    "EntryNumber": "123456Q",
           |    "EntryDate": "20201212",
           |    "DutyDetails": [
           |      {
           |        "Category": "01",
           |        "PaidAmount": "100.00",
           |        "DueAmount": "89.99"
           |      }
           |    ],
           |    "PayTo": "Importer",
           |    "PaymentDetails": {
           |      "AccountName": "ACME Importers Ltd",
           |      "AccountNumber": "71584685",
           |      "SortCode": "400530"
           |    },
           |    "ItemNumber": "1, 2, 7-10",
           |    "ClaimReason": "I believe I have been over-charged",
           |    "FirstName": "Tim",
           |    "LastName": "Tester",
           |    "SubmissionDate": "20210507"
           |  }
           |}
        |""".stripMargin)

  def createClaimAgentPayload(importerEori: String, agentEori: String): JsValue = Json.parse(s"""
       |{
       |  "AcknowledgementReference": "062c4a6ac0c34d2a810b4359aad884a5",
       |  "ApplicationType": "NIDAC",
       |  "OriginatingSystem": "Digital",
       |  "Content": {
       |    "RepresentationType": "Representative of importer",
       |    "ClaimType": "Account Sales",
       |    "ImporterDetails": {
       |      "EORI": "$importerEori",
       |      "Name": "Representatives Client Importer",
       |      "Address": {
       |        "AddressLine1": "Unit 42",
       |        "AddressLine2": "West Industrial Estate",
       |        "City": "Walsall",
       |        "PostalCode": "WS1 2AB",
       |        "CountryCode": "GB"
       |      }
       |    },
       |    "AgentDetails": {
       |      "EORI": "$agentEori",
       |      "Name": "ACME Importers Ltd",
       |      "Address": {
       |        "AddressLine1": "Unit 42",
       |        "AddressLine2": "West Industrial Estate",
       |        "City": "Walsall",
       |        "PostalCode": "WS1 2AB",
       |        "CountryCode": "GB",
       |        "TelephoneNumber": "01234567890",
       |        "EmailAddress": "tim@testing.com"
       |      }
       |    },
       |    "EntryProcessingUnit": "123",
       |    "EntryNumber": "123456Q",
       |    "EntryDate": "20201212",
       |    "DutyDetails": [
       |      {
       |        "Category": "01",
       |        "PaidAmount": "100.00",
       |        "DueAmount": "89.99"
       |      }
       |    ],
       |    "PayTo": "Importer",
       |    "PaymentDetails": {
       |      "AccountName": "ACME Importers Ltd",
       |      "AccountNumber": "71584685",
       |      "SortCode": "400530"
       |    },
       |    "ItemNumber": "1, 2, 7-10",
       |    "ClaimReason": "I believe I have been over-charged",
       |    "FirstName": "Tim",
       |    "LastName": "Tester",
       |    "SubmissionDate": "20210507"
       |  }
       |}
       |""".stripMargin)

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
    EISCreateCaseError(
      EISErrorDetail(Some("errorCode"), Some("create errorMessage"), Some("correlationId"), processingDate)
    )

  val eisCreateFailMinimumResponse: EISCreateCaseError =
    EISCreateCaseError(EISErrorDetail(None, None, Some("correlationId"), processingDate))

  val eisUpdateSuccessResponse: EISUpdateCaseSuccess =
    EISUpdateCaseSuccess("case-id", processingDate, "status", "status-text")

  val eisUpdateFailResponse: EISUpdateCaseError =
    EISUpdateCaseError(
      EISErrorDetail(Some("errorCode"), Some("update errorMessage"), Some("correlationId"), processingDate)
    )

  val eisUpdateFailMinimumResponse: EISUpdateCaseError =
    EISUpdateCaseError(EISErrorDetail(None, None, Some("correlationId"), processingDate))

}
