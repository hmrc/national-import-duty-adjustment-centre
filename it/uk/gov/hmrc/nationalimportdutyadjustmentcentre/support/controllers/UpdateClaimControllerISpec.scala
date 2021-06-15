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
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.stubs.{AuditStubs, AuthStubs, FileTransferStubs, UpdateCaseStubs}

class UpdateClaimControllerISpec
  extends ServerBaseISpec with AuthStubs with UpdateCaseStubs with FileTransferStubs {

  this: Suite with ServerProvider =>

  override implicit lazy val app: Application = defaultAppBuilder.build()

  val baseUrl = s"http://localhost:$port"

  val wsClient = app.injector.instanceOf[WSClient]

  private val body =
    """
  {
	"eisRequest": {
		"AcknowledgementReference": "22ad76437d5e4a5dbc2b142e7e4ea6f1",
		"ApplicationType": "NIDAC",
		"OriginatingSystem": "Digital",
		"Content": {
			"CaseID": "NID21134557697RM8WIB13",
			"Description": "Some new information that has been added"
		}
	},
	"uploadedFiles": [
		{
			"upscanReference": "up-123",
			"downloadUrl": "/upscan/up-123",
			"uploadTimestamp": "2021-06-14T15:22:12.080Z",
			"checksum": "identity-1",
			"fileName": "my-id.jpg",
			"fileMimeType": "image/jpeg"
		},
		{
			"upscanReference": "up-456",
			"downloadUrl": "/upscan/up-456",
			"uploadTimestamp": "2021-06-14T15:22:13.030Z",
			"checksum": "identity-2",
			"fileName": "my-scan.jpg",
			"fileMimeType": "image/jpeg"
		}
	]
}
  """
  val testRequest: JsValue = Json.parse(body)

  "POST /update-claim" should {

    "return successful response with case reference number" in {

      val correlationId = java.util.UUID.randomUUID().toString()

      givenAuthorisedAsValidTrader("my-eori")
      givenUpdateCaseResponseWithSuccessMessage()
      givenFileTransferSucceeds("NID21134557697RM8WIB13", "my-id.jpg", correlationId)
      givenFileTransferSucceeds("NID21134557697RM8WIB13", "my-scan.jpg", correlationId)

      wsClient
        .url(s"$baseUrl/update-claim")
        .withHttpHeaders("X-Correlation-ID" -> correlationId)
        .post(testRequest).futureValue


      verifyCaseUpdated(1)
      verifyFileTransferHasHappened(2)
      verifyAuditEvent()
    }


  }
}


