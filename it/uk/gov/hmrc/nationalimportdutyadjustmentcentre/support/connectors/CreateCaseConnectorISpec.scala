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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.connectors

import play.api.Application
import play.api.libs.json.{JsValue, Json}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.{HeaderCarrier, JsValidationException, UpstreamErrorResponse}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.CreateCaseConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{EISCreateCaseError, EISCreateCaseSuccess, EISErrorDetail, EISUpdateCaseSuccess}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.AppBaseISpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.stubs.CreateCaseStubs

class CreateCaseConnectorISpec extends CreateCaseConnectorISpecSetup {

  "CreateCaseConnector" when {
    "updateClaim" should {
      "return EISUpdateCaseSuccess if successful" in {

        givenCreateCaseResponseWithSuccessMessage()

        val result = await(connector.submitClaim(testRequest, correlationId))

        result mustBe EISCreateCaseSuccess(
          CaseID = caseId,
          ProcessingDate = fixedInstant,
          Status = "Success",
          StatusText = "Case created successfully"
        )
      }

      "return EISUpdateCaseError if unsuccessful" in {

        givenCreateCaseResponseWithErrorMessage(400)

        val result = await(connector.submitClaim(testRequest, correlationId))

        result mustBe EISCreateCaseError(
          EISErrorDetail(
            errorCode = Some("Some ErrorCode 999"),
            errorMessage = Some("It update case error"),
            correlationId = Some("it-correlation-id"),
            timestamp = fixedInstant
          )
        )
      }

      "retry if response indicates retry" in {

        givenCreatCaseResponseTooManyRequests()

        val result = await(connector.submitClaim(testRequest, correlationId))

        result mustBe EISCreateCaseSuccess(
          CaseID = caseId,
          ProcessingDate = fixedInstant,
          Status = "Success",
          StatusText = "Case created successfully"
        )
      }

      "return EISUpdateCaseError if no body in response" in {

        givenCreateCaseResponseWithNoBody(504)

        val result = await(connector.submitClaim(testRequest, correlationId))

        val error = result.asInstanceOf[EISCreateCaseError]
        error.errorDetail.errorCode mustBe Some("STATUS504")
        error.errorDetail.errorMessage mustBe Some("Error: empty response")
      }

      "return EISUpdateCaseError if no content type in response" in {

        givenCreateCaseResponseWithNoContentType(505)

        val result = await(connector.submitClaim(testRequest, correlationId))

        val error = result.asInstanceOf[EISCreateCaseError]
        error.errorDetail.errorCode mustBe Some("STATUS505")
        error.errorDetail.errorMessage mustBe Some("Error: missing content-type header")
      }

      "throw exception if http status is unexpected" in {

        givenCreateCaseResponseWithErrorMessage(300)

        intercept[UpstreamErrorResponse] {
          await(connector.submitClaim(testRequest, correlationId))
        }.getMessage mustBe "Unexpected response status 300"

      }

      "throw exception if content-Type is unexpected" in {

        givenCreateCaseResponseWithContentType(MimeTypes.XML)

        intercept[UpstreamErrorResponse] {
          await(connector.submitClaim(testRequest, correlationId))
        }.getMessage must include(s"expected application/json but got ${MimeTypes.XML}")

      }

      "throw exception if if invalid Json in success response" in {

        givenCreateCaseResponseWithSuccessMessage("""{"invalid": "json"}""")

        intercept[JsValidationException] {
          await(connector.submitClaim(testRequest, correlationId))
        }.getMessage must include("returned invalid json")

      }

      "throw exception if if invalid Json in failed response" in {

        givenCreateCaseResponseWithErrorMessage(400, """{"invalid": "json"}""")

        intercept[JsValidationException] {
          await(connector.submitClaim(testRequest, correlationId))
        }.getMessage must include("returned invalid json")

      }
    }

  }
}

trait CreateCaseConnectorISpecSetup extends AppBaseISpec with CreateCaseStubs {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication: Application = defaultAppBuilder.build()

  lazy val connector: CreateCaseConnector =
    app.injector.instanceOf[CreateCaseConnector]

  val correlationId = java.util.UUID.randomUUID().toString()

  val testRequest: JsValue = Json.parse("""
  {
    "AcknowledgementReference": "fe3d336eab0744be81b26a7893889652",
    "ApplicationType": "NIDAC",
    "OriginatingSystem": "Digital",
    "Content": {
      "RepresentationType": "Representative of importer",
      "ClaimType": "Account Sales",
      "ImporterDetails": {
        "EORI": "GB123456789012",
        "Name": "Ivor Importer",
        "Address": {
          "AddressLine1": "123 Shipley Rd",
          "AddressLine2": "Nr Bradford",
          "City": "Bradford",
          "PostalCode": "BD371CD",
          "CountryCode": "GB"
        }
      },
      "AgentDetails": {
        "EORI": "GB123456789000",
        "Name": "Adam Smith",
        "Address": {
          "AddressLine1": "123 High St",
          "AddressLine2": "Nr Bradford",
          "City": "Bradford",
          "PostalCode": "BD123CD",
          "CountryCode": "GB",
          "TelephoneNumber": "01234567890",
          "EmailAddress": "adam@smith.com"
        }
      },
      "EntryProcessingUnit": "010",
      "EntryNumber": "242542Q",
      "EntryDate": "20210125",
      "DutyDetails": [
        {
          "Category": "01",
          "PaidAmount": "120.15",
          "DueAmount": "99.99"
        }
      ],
      "PayTo": "Importer",
      "PaymentDetails": {
        "AccountName": "IMPORTER BANK LTD",
        "AccountNumber": "53527357",
        "SortCode": "300731"
      },
      "ItemNumber": "100-110",
      "ClaimReason": "My reasons are...",
      "FirstName": "Adam",
      "LastName": "Smith",
      "SubmissionDate": "20210414"
    }
  }
  """)

}
