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
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.UpdateCaseConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{EISErrorDetail, EISUpdateCaseError, EISUpdateCaseSuccess}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.AppBaseISpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.stubs.UpdateCaseStubs

class UpdateCaseConnectorISpec extends UpdateCaseConnectorISpecSetup {

  "UpdateCaseConnector" when {
    "updateClaim" should {
      "return EISUpdateCaseSuccess if successful" in {

        givenUpdateCaseResponseWithSuccessMessage()

        val result = await(connector.updateClaim(testRequest, correlationId))

        result mustBe EISUpdateCaseSuccess(
          CaseID = caseId,
          ProcessingDate = fixedInstant,
          Status = "Success",
          StatusText = "Case updated successfully"
        )
      }

      "return EISUpdateCaseError if unsuccessful" in {

        givenUpdateCaseResponseWithErrorMessage(400)

        val result = await(connector.updateClaim(testRequest, correlationId))

        result mustBe EISUpdateCaseError(
          EISErrorDetail(
            errorCode = Some("Some ErrorCode 999"),
            errorMessage = Some("It update case error"),
            correlationId = Some("it-correlation-id"),
            timestamp = fixedInstant
          )
        )
      }

      "return EISUpdateCaseError if no body in response" in {

        givenUpdateCaseResponseWithNoBody(504)

        val result = await(connector.updateClaim(testRequest, correlationId))

        val error = result.asInstanceOf[EISUpdateCaseError]
        error.errorDetail.errorCode mustBe Some("STATUS504")
        error.errorDetail.errorMessage mustBe Some("Error: empty response")
      }

      "return EISUpdateCaseError if no content type in response" in {

        givenUpdateCaseResponseWithNoContentType(505)

        val result = await(connector.updateClaim(testRequest, correlationId))

        val error = result.asInstanceOf[EISUpdateCaseError]
        error.errorDetail.errorCode mustBe Some("STATUS505")
        error.errorDetail.errorMessage mustBe Some("Error: missing content-type header")
      }

      "return EISUpdateCaseError if http status is unexpected" in {

        givenUpdateCaseResponseWithErrorMessage(300)

        val result = await(connector.updateClaim(testRequest, correlationId))

        val error = result.asInstanceOf[EISUpdateCaseError]
        error.errorDetail.errorCode mustBe Some("ERROR500")
        error.errorDetail.errorMessage mustBe Some("Unexpected response status 300")

      }

      "return EISUpdateCaseError if content-Type is unexpected" in {

        givenUpdateCaseResponseWithContentType(MimeTypes.XML)

        val result = await(connector.updateClaim(testRequest, correlationId))

        val error = result.asInstanceOf[EISUpdateCaseError]
        error.errorDetail.errorCode mustBe Some("ERROR500")
        error.errorDetail.errorMessage.get must include(s"expected application/json but got ${MimeTypes.XML}")

      }

      "return EISUpdateCaseError if response is plain text" in {

        givenUpdateCaseResponsePlainTextError(501, "There was a problem")

        val result = await(connector.updateClaim(testRequest, correlationId))

        val error = result.asInstanceOf[EISUpdateCaseError]
        error.errorDetail.errorCode mustBe Some("ERROR500")
        error.errorDetail.errorMessage.get must include("There was a problem")

      }

      "throw exception if if invalid Json in success response" in {

        givenUpdateCaseResponseWithSuccessMessage("""{"invalid": "json"}""")

        intercept[JsValidationException] {
          await(connector.updateClaim(testRequest, correlationId))
        }.getMessage must include ("returned invalid json")

      }

      "throw exception if if invalid Json in failed response" in {

        givenUpdateCaseResponseWithErrorMessage(400, """{"invalid": "json"}""")

        intercept[JsValidationException] {
          await(connector.updateClaim(testRequest, correlationId))
        }.getMessage must include ("returned invalid json")

      }
    }

  }
}

trait UpdateCaseConnectorISpecSetup extends AppBaseISpec with UpdateCaseStubs {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication: Application = defaultAppBuilder.build()

  lazy val connector: UpdateCaseConnector =
    app.injector.instanceOf[UpdateCaseConnector]

  val correlationId = java.util.UUID.randomUUID().toString()

  val testRequest: JsValue = Json.parse("""
  {
    "AcknowledgementReference": "b9cea592cf4641d08a4ee92da7036950",
    "ApplicationType": "NIDAC",
    "OriginatingSystem": "Digital",
    "Content": {
      "CaseID": "NID21134557697RM8WIB13",
      "Description": "extra additional information"
    }
  }
  """)

}
