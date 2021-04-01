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

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verifyNoInteractions, when}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.ControllerSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.{MicroserviceAuthConnector, UpdateCaseConnector}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.ApiError
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.services.FileTransferService
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.utils.TestData

import java.time.ZonedDateTime
import scala.concurrent.Future

class UpdateClaimControllerSpec extends ControllerSpec with GuiceOneAppPerSuite with TestData {

  private val mockUpdateCaseConnector = mock[UpdateCaseConnector]
  private val mockFileTransferService = mock[FileTransferService]

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(
      bind[MicroserviceAuthConnector].to(mockAuthConnector),
      bind[UpdateCaseConnector].to(mockUpdateCaseConnector),
      bind[FileTransferService].to(mockFileTransferService)
    )
    .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    withAuthorizedUser()
  }

  override protected def afterEach(): Unit = {
    reset(mockUpdateCaseConnector)
    super.afterEach()
  }

  "Update" should {

    val updatePost = FakeRequest("POST", "/update-claim")

    "update-case request succeeds with no files to upload" in {
      when(mockUpdateCaseConnector.updateClaim(any[JsValue], anyString())(any())).thenReturn(
        Future.successful(eisUpdateSuccessResponse)
      )
      when(mockFileTransferService.transferFiles(any(), any(), any())(any())).thenReturn(Future.successful(Seq.empty))
      val result: Future[Result] =
        route(app, updatePost.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(updateClaimRequest))).get

      status(result) must be(OK)
      contentAsJson(result) mustBe toJson(
        UpdateClaimResponse(
          correlationId = "xyz",
          processingDate = Some("2021-12-03T10:15:30"),
          error = None,
          result = Some(UpdateClaimResult(eisUpdateSuccessResponse.CaseID, Seq.empty))
        )
      )
    }

    "update-case request succeeds and file uploads succeed" in {
      when(mockUpdateCaseConnector.updateClaim(any[JsValue], anyString())(any())).thenReturn(
        Future.successful(eisUpdateSuccessResponse)
      )

      val uploads = uploadedFiles("upscanReference")
      val fileTransferResults = Seq(
        FileTransferResult(
          upscanReference = "upscanReference",
          success = true,
          httpStatus = 202,
          transferredAt = ZonedDateTime.now.toLocalDateTime
        )
      )

      when(mockFileTransferService.transferFiles(any(), any(), any())(any())).thenReturn(
        Future.successful(fileTransferResults)
      )

      val request = updateClaimRequest.copy(uploadedFiles = uploads)

      val result: Future[Result] =
        route(app, updatePost.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(request))).get

      status(result) must be(OK)
      contentAsJson(result) mustBe toJson(
        UpdateClaimResponse(
          correlationId = "xyz",
          processingDate = Some("2021-12-03T10:15:30"),
          error = None,
          result = Some(UpdateClaimResult(eisUpdateSuccessResponse.CaseID, fileTransferResults))
        )
      )
    }

    "handle an unsuccessful request" when {
      "request fails" in {
        when(mockUpdateCaseConnector.updateClaim(any[JsValue], anyString())(any())).thenReturn(
          Future.successful(eisUpdateFailResponse)
        )
        val result: Future[Result] =
          route(app, updatePost.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(updateClaimRequest))).get

        status(result) must be(OK)
        contentAsJson(result) mustBe toJson(
          UpdateClaimResponse(
            correlationId = "xyz",
            processingDate = Some("2021-12-04T10:15:30"),
            error = Some(ApiError(eisUpdateFailResponse.ErrorCode, Some(eisUpdateFailResponse.ErrorMessage))),
            result = None
          )
        )
      }
    }

    "handle an invalid request" when {
      "request is invalid" in {
        val result: Future[Result] = route(app, updatePost.withJsonBody(Json.obj("field" -> "value"))).get

        status(result) must be(BAD_REQUEST)
        verifyNoInteractions(mockUpdateCaseConnector)
      }
    }
  }
}
