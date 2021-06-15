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
import com.github.tomakehurst.wiremock.stubbing.Scenario
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.WireMockSupport

trait AuditStubs {
  me: WireMockSupport =>

  val AUDIT_URL = "/write/audit"
  val AUDIT_MERGED_URL = "/write/audit/merged"

  def givenFileTransferAuditted(): Unit = {

    stubForPostWithResponse(204)
    stubForImplicitAudits(204)

  }
  def verifyAuditEvent(times: Int = 1) =
    verify(times, postRequestedFor(urlPathEqualTo(AUDIT_URL)))

  private def stubForPostWithResponse(status: Int): Unit =
    stubFor(
      post(urlEqualTo(AUDIT_URL))
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )

  private def stubForImplicitAudits(status: Int): Unit =
    stubFor(
      post(urlEqualTo(AUDIT_MERGED_URL))
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )

}
