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

import com.github.tomakehurst.wiremock.client.CountMatchingStrategy.EQUAL_TO
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.WireMockSupport

trait AuditStubs extends Eventually {
  me: WireMockSupport =>

  override implicit val patienceConfig =
    PatienceConfig(scaled(Span(5, Seconds)), scaled(Span(500, Millis)))


  def verifyAuditEvent() =
    eventually(verify(postRequestedFor(urlPathMatching(auditUrl))))

  def verifyAuditEvent(times: Int) =
    eventually(verify(times, postRequestedFor(urlPathMatching(auditUrl))))

  def verifyFilesTransferredAudit(times: Int) =
    eventually(verify(times, postRequestedFor(urlPathMatching(auditUrl)).withRequestBody(matchingJsonPath("$.auditType", containing("FilesTransferred")))))

  def verifyFileTransfersAuditedSuccessAndFailures(times: Int) =
    eventually(verify(times, postRequestedFor(urlPathMatching(auditUrl))
      .withRequestBody(matchingJsonPath("$.auditType", containing("FilesTransferred")))
      .withRequestBody(matchingJsonPath("$.detail.fileTransferResults[*].success", containing("false")))
      .withRequestBody(matchingJsonPath("$.detail.fileTransferResults[*].success", containing("true")))
      .withRequestBody(matchingJsonPath("$.detail.fileTransferResults[*].httpStatus", containing("499")))
      .withRequestBody(matchingJsonPath("$.detail.fileTransferResults[*].error", containing("Cannot determine success status")))
    ))

  def givenAuditConnector(): Unit =
    stubFor(post(urlPathMatching(auditUrl)).willReturn(aResponse().withStatus(204)))

  private def auditUrl = "/write/audit.*"

}
