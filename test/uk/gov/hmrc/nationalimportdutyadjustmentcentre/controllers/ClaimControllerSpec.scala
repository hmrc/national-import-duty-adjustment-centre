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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verifyNoInteractions, when}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.ControllerSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.MicroserviceAuthConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{CreateClaimRequest, CreateClaimResponse}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.services.ClaimService

import scala.concurrent.Future

class ClaimControllerSpec extends ControllerSpec with GuiceOneAppPerSuite {

  private val claimRequest = CreateClaimRequest("some-id", "some-claim-type")

  private val mockClaimService = mock[ClaimService]
  private val fakeResponse     = CreateClaimResponse("id", "claim-reference")

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[MicroserviceAuthConnector].to(mockAuthConnector), bind[ClaimService].to(mockClaimService))
    .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    withAuthorizedUser()
    when(mockClaimService.create(any[CreateClaimRequest])).thenReturn(Future.successful(fakeResponse))
  }

  override protected def afterEach(): Unit = {
    reset(mockClaimService)
    super.afterEach()
  }

  "Create" should {

    val post = FakeRequest("POST", "/create-claim")

    "return 201" when {

      "request is valid" in {
        val result: Future[Result] = route(app, post.withJsonBody(toJson(claimRequest))).get

        status(result) must be(CREATED)
        contentAsJson(result) mustBe toJson(fakeResponse)
      }
    }

    "return 400" when {
      "request is invalid" in {
        val result: Future[Result] = route(app, post.withJsonBody(Json.obj("field" -> "value"))).get

        status(result) must be(BAD_REQUEST)
        verifyNoInteractions(mockClaimService)
      }
    }
  }
}
