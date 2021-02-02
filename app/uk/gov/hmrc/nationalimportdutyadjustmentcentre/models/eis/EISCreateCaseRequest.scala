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

import java.time.format.DateTimeFormatter

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.CreateClaimRequest

/**
  * Create specified case in the PEGA system.
  * Based on spec "CSG_NIDAC_AutoCreateCase_API_Spec_V0.2.docx"  (NOTE: PEGA spec)
  */
case class EISCreateCaseRequest(
  AcknowledgementReference: String,
  ApplicationType: String,
  OriginatingSystem: String,
  Content: EISCreateCaseRequest.Content
)

object EISCreateCaseRequest {
  implicit val formats: Format[EISCreateCaseRequest] = Json.format[EISCreateCaseRequest]

  case class Content(
    ClaimType: String,
    ImporterDetails: ImporterDetails,
    EntryProcessingUnit: String,
    EntryNumber: String,
    EntryDate: String,
    DutyDetails: Seq[DutyDetail],
    PaymentDetails: Option[PaymentDetails],
    FirstName: String,
    LastName: String
  )

  object Content {
    implicit val formats: Format[Content] = Json.format[Content]

    val entryDataFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    def apply(request: CreateClaimRequest): Content =
      Content(
        ClaimType = request.claimType,
        ImporterDetails = ImporterDetails(request.contactDetails, request.importerAddress),
        EntryProcessingUnit = request.entryDetails.entryProcessingUnit,
        EntryNumber = request.entryDetails.entryNumber,
        EntryDate = entryDataFormatter.format(request.entryDetails.entryDate),
        // TODO - remove hard-coded values for paid and due amounts
        DutyDetails = request.reclaimDutyTypes.map(value => DutyDetail(value, "0", "0")).toSeq,
        PaymentDetails = request.bankDetails.map(PaymentDetails(_)),
        FirstName = request.contactDetails.firstName,
        LastName = request.contactDetails.lastName
      )

  }

}
