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
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.WireMockSupport

trait UpdateCaseStubs {
  me: WireMockSupport =>

  val UPDATE_CASE_URL = "/eis-stub/update-case"

  def givenUpdateCaseRequestSucceeds(caseId: String): Unit =
    stubForPostWithResponse(
      200,
      s"""{
        |    "Status": "Success",
        |    "StatusText": "Case Updated successfully",
        |    "CaseID": "$caseId",
        |    "ProcessingDate": "$fixedInstant"
        |}""".stripMargin
    )

  def givenUpdateCaseRequestFails(): Unit =
    stubForPostWithResponse(
      400,
      s"""{
         |    "ProcessingDate": "$fixedInstant",
         |    "CorrelationID": "it-correlation-id",
         |    "ErrorCode": "999",
         |    "ErrorMessage": "It update case error"
         |}""".stripMargin
    )

  private def stubForPostWithResponse(status: Int, responseBody: String): Unit =
    stubFor(
      post(urlEqualTo(UPDATE_CASE_URL))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        )
    )


}
