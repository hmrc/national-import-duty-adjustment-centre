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

import java.util.UUID

import play.api.Application
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.FileTransferConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.TraderServicesFileTransferRequest
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.AppBaseISpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.stubs.FileTransferStubs

class FileTransferConnectorISpec extends FileTransferConnectorISpecSetup {

  "FileTransferConnectorISpec" when {
    "transferFile" should {
      "return success if successful" in {

        givenTraderServicesFileTransfer()

        val request = testRequest(UUID.randomUUID().toString)
        val result  = await(connector.transferFile(request))

        result.upscanReference mustBe request.upscanReference
        result.success mustBe true
        result.httpStatus mustBe 202

        verifyTraderServicesFileTransferHasHappened(times = 1)
      }

      "return failure for non-2xx response" in {

        givenTraderServicesFileTransferWithStatus(403)

        val request = testRequest(UUID.randomUUID().toString)
        val result  = await(connector.transferFile(request))

        result.upscanReference mustBe request.upscanReference
        result.success mustBe false
        result.httpStatus mustBe 403
        result.error mustBe None

        verifyTraderServicesFileTransferHasHappened(times = 1)
      }

    }
  }
}

trait FileTransferConnectorISpecSetup extends AppBaseISpec with FileTransferStubs {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication: Application = defaultAppBuilder.build()

  lazy val connector: FileTransferConnector =
    app.injector.instanceOf[FileTransferConnector]

  def testRequest(upscanReference: String): TraderServicesFileTransferRequest = TraderServicesFileTransferRequest(
    conversationId = "",
    caseReferenceNumber = "NID21134557697RM8WIB13",
    applicationName = "",
    upscanReference = upscanReference,
    downloadUrl = "",
    checksum = "",
    fileName = "",
    fileMimeType = "",
    batchSize = 1,
    batchCount = 1
  )

}
