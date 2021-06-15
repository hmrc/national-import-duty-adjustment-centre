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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.controllers

import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.Application
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.ServerBaseISpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.stubs.{AuthStubs, CreateCaseStubs, FileTransferStubs, UpdateCaseStubs}

class CreateClaimControllerISpec
  extends ServerBaseISpec with AuthStubs with CreateCaseStubs with FileTransferStubs {

  this: Suite with ServerProvider =>

  override implicit lazy val app: Application = defaultAppBuilder.build()

  val baseUrl = s"http://localhost:$port"

  val wsClient = app.injector.instanceOf[WSClient]

  private val createBody =
  """
  {
	"eisRequest": {
		"AcknowledgementReference": "41f8944f23a645c987e3ddbe4663b944",
		"ApplicationType": "NIDAC",
		"OriginatingSystem": "Digital",
		"Content": {
			"RepresentationType": "Importer",
			"ClaimType": "Account Sales",
			"ImporterDetails": {
				"EORI": "GB123456789000",
				"Name": "ACME Importers Ltd",
				"Address": {
					"AddressLine1": "1 Test Street",
					"City": "Testtown",
					"PostalCode": "AA00 0AA",
					"CountryCode": "GB",
					"TelephoneNumber": "01234567890",
					"EmailAddress": "tim@testing.com"
				}
			},
			"EntryProcessingUnit": "123",
			"EntryNumber": "123456Q",
			"EntryDate": "20201212",
			"DutyDetails": [
				{
					"Category": "01",
					"PaidAmount": "100.00",
					"DueAmount": "89.99"
				},
				{
					"Category": "02",
					"PaidAmount": "80.00",
					"DueAmount": "72.50"
				},
				{
					"Category": "03",
					"PaidAmount": "50.00",
					"DueAmount": "49.99"
				}
			],
			"PayTo": "Importer",
			"PaymentDetails": {
				"AccountName": "ACME Importers Ltd",
				"AccountNumber": "71584685",
				"SortCode": "400530"
			},
			"ItemNumber": "1, 2, 7-10",
			"ClaimReason": "I believe I have been over-charged",
			"FirstName": "Tim",
			"LastName": "Tester",
			"SubmissionDate": "20210614"
		}
	},
	"uploadedFiles": [
		{
			"upscanReference": "up-abc",
			"downloadUrl": "/upscan/up-def",
			"uploadTimestamp": "2021-06-14T19:42:42.920Z",
			"checksum": "valid-73",
			"fileName": "my-form.pdf",
			"fileMimeType": "application/pdf"
		}
	]
}
  """
  val testRequest: JsValue = Json.parse(createBody)

  "POST /create-claim" should {

    "return successful response with case reference number" in {

      val correlationId = java.util.UUID.randomUUID().toString()

      givenAuthorisedAsValidTrader("GB123456789000")
      givenCreateCaseResponseWithSuccessMessage()
      givenTraderServicesFileTransferSucceeds("NID21134557697RM8WIB13", "my-form.pdf", correlationId)

      wsClient
        .url(s"$baseUrl/create-claim")
        .withHttpHeaders("X-Correlation-ID" -> correlationId)
        .post(testRequest).futureValue


      verifyCaseCreated(1)
      verifyTraderServicesFileTransferHasHappened(1)
    }


  }
}


