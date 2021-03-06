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

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
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
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.ApiError
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.services.FileTransferService
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.utils.TestData

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
    withAuthEnrolments(validEnrolments())
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
      when(
        mockFileTransferService.transferFiles(
          ArgumentMatchers.eq(eisUpdateSuccessResponse.CaseID),
          ArgumentMatchers.eq("xyz"),
          ArgumentMatchers.eq(updateClaimRequest.uploadedFiles)
        )(any(), any())
      ).thenReturn(Future(Seq.empty))
      val result: Future[Result] =
        route(app, updatePost.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(updateClaimRequest))).get

      status(result) must be(OK)
      contentAsJson(result) mustBe toJson(
        UpdateClaimResponse(
          correlationId = "xyz",
          processingDate = Some(processingDate),
          error = None,
          result = Some(UpdateClaimResult(eisUpdateSuccessResponse.CaseID, Seq.empty))
        )
      )

      verify(mockFileTransferService).transferFiles(
        ArgumentMatchers.eq(eisUpdateSuccessResponse.CaseID),
        ArgumentMatchers.eq("xyz"),
        ArgumentMatchers.eq(updateClaimRequest.uploadedFiles)
      )(any(), any())
    }

    "update-case request succeeds and file uploads succeed" in {
      when(mockUpdateCaseConnector.updateClaim(any[JsValue], anyString())(any())).thenReturn(
        Future.successful(eisUpdateSuccessResponse)
      )

      val uploads = uploadedFiles("upscanReference")

      when(
        mockFileTransferService.transferFiles(
          ArgumentMatchers.eq(eisUpdateSuccessResponse.CaseID),
          ArgumentMatchers.eq("xyz"),
          ArgumentMatchers.eq(uploads)
        )(any(), any())
      ).thenReturn(Future(Seq.empty))

      val fileTransferResults = Seq.empty

      val request = updateClaimRequest.copy(uploadedFiles = uploads)

      val result: Future[Result] =
        route(app, updatePost.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(request))).get

      status(result) must be(OK)
      contentAsJson(result) mustBe toJson(
        UpdateClaimResponse(
          correlationId = "xyz",
          processingDate = Some(processingDate),
          error = None,
          result = Some(UpdateClaimResult(eisUpdateSuccessResponse.CaseID, fileTransferResults))
        )
      )

      verify(mockFileTransferService)
        .transferFiles(
          ArgumentMatchers.eq(eisUpdateSuccessResponse.CaseID),
          ArgumentMatchers.eq("xyz"),
          ArgumentMatchers.eq(uploads)
        )(any(), any())
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
            processingDate = Some(processingDate),
            error = Some(
              ApiError(eisUpdateFailResponse.errorDetail.errorCode, eisUpdateFailResponse.errorDetail.errorMessage)
            ),
            result = None
          )
        )
      }
      "minimum EIS response is returned" in {
        when(mockUpdateCaseConnector.updateClaim(any[JsValue], anyString())(any())).thenReturn(
          Future.successful(eisUpdateFailMinimumResponse)
        )
        val result: Future[Result] =
          route(app, updatePost.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(updateClaimRequest))).get

        status(result) must be(OK)
        contentAsJson(result) mustBe toJson(
          UpdateClaimResponse(
            correlationId = "xyz",
            processingDate = Some(processingDate),
            error = Some(ApiError("NONE-SUPPLIED")),
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
