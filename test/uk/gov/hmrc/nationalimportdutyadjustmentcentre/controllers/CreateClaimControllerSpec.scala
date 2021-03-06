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
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.ForbiddenException
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.ControllerSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.config.AppConfig
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.MicroserviceAuthConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.ApiError
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{CreateClaimResponse, CreateClaimResult}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.services.{CreateCaseService, FileTransferService}

import scala.concurrent.Future

class CreateClaimControllerSpec extends ControllerSpec with GuiceOneAppPerSuite {

  private val mockCreateCaseService   = mock[CreateCaseService]
  private val mockFileTransferService = mock[FileTransferService]

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(
      bind[MicroserviceAuthConnector].to(mockAuthConnector),
      bind[CreateCaseService].to(mockCreateCaseService),
      bind[FileTransferService].to(mockFileTransferService),
      bind[AppConfig].to(mockAppConfig)
    )
    .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    withAuthorizedUser()
    withAuthEnrolments(validEnrolments())
    withAppConfigAllowEoriNumber(validEORI)
    withAppConfigEoriEnrolments(Seq("HMRC-CTS-ORG"))
  }

  override protected def afterEach(): Unit = {
    reset(mockCreateCaseService)
    super.afterEach()
  }

  "Create" should {

    val post = FakeRequest("POST", "/create-claim")

    "handle a successful request" when {

      "create-case request succeeds with no files to upload" in {
        when(mockCreateCaseService.submitClaim(anyString(), any[JsValue], anyString())(any())).thenReturn(
          Future.successful(eisCreateSuccessResponse)
        )

        when(
          mockFileTransferService.transferFiles(
            ArgumentMatchers.eq(eisCreateSuccessResponse.CaseID),
            ArgumentMatchers.eq("xyz"),
            ArgumentMatchers.eq(createClaimRequest.uploadedFiles)
          )(any(), any())
        ).thenReturn(Future(Seq.empty))

        val result: Future[Result] =
          route(app, post.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(createClaimRequest))).get

        status(result) must be(OK)
        contentAsJson(result) mustBe toJson(
          CreateClaimResponse(
            correlationId = "xyz",
            processingDate = Some(processingDate),
            error = None,
            result = Some(CreateClaimResult(eisCreateSuccessResponse.CaseID, Seq.empty))
          )
        )

        verify(mockFileTransferService).transferFiles(
          ArgumentMatchers.eq(eisCreateSuccessResponse.CaseID),
          ArgumentMatchers.eq("xyz"),
          ArgumentMatchers.eq(createClaimRequest.uploadedFiles)
        )(any(), any())

      }

      "create-case request succeeds and file uploads succeed" in {
        when(mockCreateCaseService.submitClaim(anyString(), any[JsValue], anyString())(any())).thenReturn(
          Future.successful(eisCreateSuccessResponse)
        )

        val uploads = uploadedFiles("upscanReference")

        when(
          mockFileTransferService.transferFiles(
            ArgumentMatchers.eq(eisCreateSuccessResponse.CaseID),
            ArgumentMatchers.eq("xyz"),
            ArgumentMatchers.eq(uploads)
          )(any(), any())
        ).thenReturn(Future(Seq.empty))

        val fileTransferResults = Seq.empty

        val request = createClaimRequest.copy(uploadedFiles = uploads)

        val result: Future[Result] =
          route(app, post.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(request))).get

        status(result) must be(OK)
        contentAsJson(result) mustBe toJson(
          CreateClaimResponse(
            correlationId = "xyz",
            processingDate = Some(processingDate),
            error = None,
            result = Some(CreateClaimResult(eisCreateSuccessResponse.CaseID, fileTransferResults))
          )
        )

        verify(mockFileTransferService).transferFiles(
          ArgumentMatchers.eq(eisCreateSuccessResponse.CaseID),
          ArgumentMatchers.eq("xyz"),
          ArgumentMatchers.eq(uploads)
        )(any(), any())

      }

    }

    "handle an unsuccessful request" when {

      "request fails" in {
        when(mockCreateCaseService.submitClaim(anyString(), any[JsValue], anyString())(any())).thenReturn(
          Future.successful(eisCreateFailResponse)
        )
        val result: Future[Result] =
          route(app, post.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(createClaimRequest))).get

        status(result) must be(OK)
        contentAsJson(result) mustBe toJson(
          CreateClaimResponse(
            correlationId = "xyz",
            processingDate = Some(processingDate),
            error = Some(
              ApiError(eisCreateFailResponse.errorDetail.errorCode, eisCreateFailResponse.errorDetail.errorMessage)
            ),
            result = None
          )
        )
      }

      "minimum EIS response is returned" in {
        when(mockCreateCaseService.submitClaim(anyString(), any[JsValue], anyString())(any())).thenReturn(
          Future.successful(eisCreateFailMinimumResponse)
        )
        val result: Future[Result] =
          route(app, post.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(createClaimRequest))).get

        status(result) must be(OK)
        contentAsJson(result) mustBe toJson(
          CreateClaimResponse(
            correlationId = "xyz",
            processingDate = Some(processingDate),
            error = Some(ApiError("NONE-SUPPLIED")),
            result = None
          )
        )
      }

      "service throws ForbiddenException" in {
        when(mockCreateCaseService.submitClaim(anyString(), any[JsValue], anyString())(any())).thenReturn(
          Future.failed(new ForbiddenException("Not allowed here"))
        )
        val result: Future[Result] =
          route(app, post.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(createClaimRequest))).get

        status(result) must be(FORBIDDEN)
        contentAsJson(result) mustBe Json.obj("statusCode" -> FORBIDDEN, "message" -> "Not allowed here")
      }

    }

    "handle an invalid request" when {

      "request is invalid" in {
        val result: Future[Result] =
          route(app, post.withHeaders(("x-correlation-id", "xyz")).withJsonBody(Json.obj("field" -> "value"))).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include("Invalid CreateEISClaimRequest payload")
        verifyNoInteractions(mockCreateCaseService)
      }

      "request is missing x-correlation-id" in {
        val result: Future[Result] =
          route(app, post.withJsonBody(toJson(createClaimRequest))).get

        status(result) must be(BAD_REQUEST)
        contentAsJson(result) mustBe Json.obj(
          "statusCode" -> BAD_REQUEST,
          "message"    -> "Missing header x-correlation-id"
        )
        verifyNoInteractions(mockCreateCaseService)
      }

    }

    "is unauthorised" when {
      "does not have any enrolments" in {
        withAuthEnrolments(Enrolments(Set.empty))
        userIsUnauthorised(post)
      }

      "does not have correct enrolment" in {
        withAppConfigEoriEnrolments(Seq("HMRC-NEW-ORG"))
        userIsUnauthorised(post)
      }

      "does not have allowed EORI number" in {
        withAppConfigAllowEoriNumber(validEORI, false)
        userIsUnauthorised(post)
      }

      "user has no bearer token" in {
        withUnauthorizedUser()
        userIsUnauthorised(post)
      }

      "user is an individual" in {
        withIndividualUser()
        userIsUnauthorised(post)
      }
    }
  }

  private def userIsUnauthorised(post: FakeRequest[AnyContentAsEmpty.type]) = {
    val result: Future[Result] =
      route(app, post.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(createClaimRequest))).get

    status(result) must be(UNAUTHORIZED)
    contentAsJson(result) mustBe Json.obj("statusCode" -> UNAUTHORIZED, "message" -> "Invalid user")
    verifyNoInteractions(mockCreateCaseService)
  }

}
