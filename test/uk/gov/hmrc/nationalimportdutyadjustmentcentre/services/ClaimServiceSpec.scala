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

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.UnitSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.CreateCaseConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{EISCreateCaseRequest, EISCreateCaseSuccess}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.utils.TestData

import scala.concurrent.{ExecutionContext, Future}

class ClaimServiceSpec extends UnitSpec with ScalaFutures with TestData {

  implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  val connector: CreateCaseConnector = mock[CreateCaseConnector]
  val service: ClaimService          = new ClaimService(connector)

  val connectorSuccessResponse: EISCreateCaseSuccess = mock[EISCreateCaseSuccess]

  "ClaimService" should {

    "return EIS response" when {

      "create called" in {

        when(connector.submitClaim(any[EISCreateCaseRequest], anyString())(any())).thenReturn(
          Future.successful(connectorSuccessResponse)
        )
        val response = service.createClaim(eisCreateCaseRequest(claimRequest), "xyz").futureValue

        response must be(connectorSuccessResponse)
      }
    }
  }
}
