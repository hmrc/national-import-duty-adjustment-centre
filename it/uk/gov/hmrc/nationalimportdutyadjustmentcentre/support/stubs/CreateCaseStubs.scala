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
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.support.WireMockSupport

trait CreateCaseStubs {
  me: WireMockSupport =>

  val UPDATE_CASE_URL = "/eis-stub/create-case"

  val caseId = "NID21134557697RM8WIB13"

  private val successResponseJson =
    s"""{
       |    "Status": "Success",
       |    "StatusText": "Case created successfully",
       |    "CaseID": "$caseId",
       |    "ProcessingDate": "$fixedInstant"
       |}""".stripMargin

  private val errorResponseJson =
    s"""{
       |    "errorDetail": {
       |      "timestamp": "$fixedInstant",
       |      "correlationId": "it-correlation-id",
       |      "errorCode": "Some ErrorCode 999",
       |      "errorMessage": "It update case error"
       |    }
       |}""".stripMargin

  def givenCreateCaseResponseWithSuccessMessage(responseBody: String = successResponseJson): Unit =
    stubForPostWithResponse(200, responseBody)

  def givenCreateCaseResponseWithErrorMessage(status: Int, responseBody: String = errorResponseJson): Unit =
    stubForPostWithResponse(status, responseBody)

  def givenCreateCaseResponseWithNoBody(status: Int): Unit = stubFor(
    post(urlEqualTo(UPDATE_CASE_URL))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", MimeTypes.JSON)
      )
  )

  def givenCreateCaseResponseWithNoContentType(status: Int): Unit = stubFor(
    post(urlEqualTo(UPDATE_CASE_URL))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(errorResponseJson)
      )
  )

  def givenCreateCaseResponseWithContentType(contentType: String): Unit =
    stubForPostWithResponse(200, successResponseJson, contentType)

  private def stubForPostWithResponse(status: Int, responseBody: String, contentType: String = MimeTypes.JSON): Unit =
    stubFor(
      post(urlEqualTo(UPDATE_CASE_URL))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", contentType)
            .withBody(responseBody)
        )
    )

}
