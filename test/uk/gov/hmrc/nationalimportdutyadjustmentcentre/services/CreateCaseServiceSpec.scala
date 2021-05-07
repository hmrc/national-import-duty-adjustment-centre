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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException, UpstreamErrorResponse}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.UnitSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.CreateCaseConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.utils.TestData

import scala.concurrent.{ExecutionContext, Future}

class CreateCaseServiceSpec extends UnitSpec with ScalaFutures with BeforeAndAfterEach with TestData {

  implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockConnector: CreateCaseConnector = mock[CreateCaseConnector]
  val service: CreateCaseService         = new CreateCaseService(mockConnector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockConnector.submitClaim(any(), any())(any())).thenReturn(Future.successful(eisCreateSuccessResponse))
  }

  override protected def afterEach(): Unit = {
    reset(mockConnector)
    super.afterEach()
  }

  "CreateCaseService" should {
    "return success response" when {
      "valid importer claim request is submitted" in {

        val createCaseResponse =
          service.submitClaim(importerEORI, createClaimImporterPayload(importerEORI), "correlationId").futureValue

        createCaseResponse mustBeeisCreateSuccessResponse
      }

      "valid agent claim request is submitted" in {

        val createCaseResponse =
          service.submitClaim(agentEORI, createClaimAgentPayload(importerEORI, agentEORI), "correlationId").futureValue

        createCaseResponse mustBeeisCreateSuccessResponse
      }
    }

    "return unauthorised" when {
      "importer claim is submitted with incorrect EORI" in {

        intercept[UnauthorizedException] {
          await(service.submitClaim(agentEORI, createClaimImporterPayload(importerEORI), "correlationId"))
        }.getMessage mustBe "Bad user"
      }

      "agent claim is submitted with incorrect EORI" in {

        intercept[UnauthorizedException] {
          await(service.submitClaim(importerEORI, createClaimAgentPayload(importerEORI, agentEORI), "correlationId"))
        }.getMessage mustBe "Bad user"
      }
    }
  }
}
