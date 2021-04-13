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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.UpdateCaseConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{EISUpdateCaseError, EISUpdateCaseSuccess}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.AppBaseISpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.stubs.UpdateCaseStubs

class UpdateCaseConnectorISpec extends UpdateCaseConnectorISpecSetup {

  "UpdateCaseConnector" when {
    "updateClaim" should {
      "return EISUpdateCaseSuccess if successful" in {

        givenUpdateCaseRequestSucceeds("NID21134557697RM8WIB13")

        val result = await(connector.updateClaim(testRequest, correlationId))

        result mustBe EISUpdateCaseSuccess(
          CaseID =  "NID21134557697RM8WIB13",
          ProcessingDate = fixedInstant,
          Status = "Success",
          StatusText = "Case Updated successfully"
        )
      }

      "return EISUpdateCaseError if unsuccessful" in {

        givenUpdateCaseRequestFails()

        val result = await(connector.updateClaim(testRequest, correlationId))

        result mustBe EISUpdateCaseError(
          ErrorCode = "999",
          ErrorMessage = "It update case error",
          CorrelationID = Some("it-correlation-id"),
          ProcessingDate = Some(fixedInstant)
        )
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
    "eisRequest": {
      "AcknowledgementReference": "a6597acbc47d4cad991f7f6e27e0df0f",
      "ApplicationType": "NIDAC",
      "OriginatingSystem": "Digital",
      "Content": {
        "CaseID": "NID21134557697RM8WIB13",
        "Description": "Some new information that has been added"
      }
    },
    "uploadedFiles": []
  }
  """)

}
