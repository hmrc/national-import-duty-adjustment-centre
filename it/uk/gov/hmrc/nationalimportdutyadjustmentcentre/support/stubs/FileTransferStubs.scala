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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.WireMockSupport

trait FileTransferStubs {
  me: WireMockSupport =>

  val FILE_TRANSFER_URL = "/file-transmission-synchronous-stub/transfer-file"

  def givenFileTransferSucceeds(
                                               caseReferenceNumber: String,
                                               fileName: String,
                                               conversationId: String
                                             ): Unit = {

    stubFor(
      post(urlPathEqualTo(FILE_TRANSFER_URL))
        .withRequestBody(
          equalToJson(
            s"""{
               |   "caseReferenceNumber":"$caseReferenceNumber",
               |   "fileName":"$fileName",
               |   "conversationId":"$conversationId"
               |}""".stripMargin,
            true,
            true
          )
        )
        .willReturn(
          aResponse()
            .withStatus(202)
        )
    )
  }

  def givenFileTransfer(): Unit =
    stubFor(
      post(urlPathEqualTo(FILE_TRANSFER_URL))
        .willReturn(
          aResponse()
            .withStatus(202)
        )
    )


  def givenFileTransferWithStatus(status: Int): Unit =
    stubFor(
      post(urlPathEqualTo(FILE_TRANSFER_URL))
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )

  def givenBrokenFileTransfer(): Unit =
    stubFor(
      post(urlPathEqualTo(FILE_TRANSFER_URL))
        .willReturn(
          aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER)
        )
    )

  def givenFileTransferGivesFourNineNine(
                                          caseReferenceNumber: String,
                                          conversationId: String
                                        ): Unit = {
    stubFor(
      post(urlEqualTo(FILE_TRANSFER_URL))
        .withRequestBody(
          equalToJson(
            s"""{
               |   "caseReferenceNumber":"$caseReferenceNumber",
               |   "fileName":"works.jpg",
               |   "conversationId":"$conversationId"
               |}""".stripMargin,
            true,
            true
          )
        )
        .willReturn(aResponse()
          .withStatus(202))
    )

    stubFor(
      post(urlEqualTo(FILE_TRANSFER_URL))
        .withRequestBody(
          equalToJson(
            s"""{
               |   "caseReferenceNumber":"$caseReferenceNumber",
               |   "fileName":"fails.jpg",
               |   "conversationId":"$conversationId"
               |}""".stripMargin,
            true,
            true
          )
        )
        .willReturn(aResponse().withStatus(499))
    )
  }

  def verifyFileTransferHasHappened(times: Int = 1) =
    verify(times, postRequestedFor(urlPathEqualTo(FILE_TRANSFER_URL)))
}
