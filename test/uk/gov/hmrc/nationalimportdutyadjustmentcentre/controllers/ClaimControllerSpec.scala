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
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.ControllerSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.MicroserviceAuthConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{ApiError, EISCreateCaseRequest}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{CreateClaimRequest, CreateClaimResponse, CreateClaimResult, FileTransferResult, UploadedFile}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.services.{ClaimService, FileTransferService}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.utils.TestData

import java.time.ZonedDateTime
import scala.concurrent.Future

class ClaimControllerSpec extends ControllerSpec with GuiceOneAppPerSuite with TestData {

  private val claimRequest = CreateClaimRequest("some-id", "some-claim-type", Seq.empty)

  private val mockClaimService = mock[ClaimService]
  private val mockFileTransferService = mock[FileTransferService]

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[MicroserviceAuthConnector].to(mockAuthConnector), bind[ClaimService].to(mockClaimService), bind[FileTransferService].to(mockFileTransferService))
    .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    withAuthorizedUser()
  }

  override protected def afterEach(): Unit = {
    reset(mockClaimService)
    super.afterEach()
  }

  "Create" should {

    val post = FakeRequest("POST", "/create-claim")

    "return 201" when {

      "create-case request succeeds with no files to upload" in {
        when(mockClaimService.createClaim(any[EISCreateCaseRequest], anyString())(any())).thenReturn(
          Future.successful(eisSuccessResponse)
        )
        when(mockFileTransferService.transfer(eisSuccessResponse.CaseID, Seq.empty)).thenReturn(
          Future.successful(Seq.empty)
        )
        val result: Future[Result] =
          route(app, post.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(claimRequest))).get

        status(result) must be(CREATED)
        contentAsJson(result) mustBe toJson(
          CreateClaimResponse(
            correlationId = "xyz",
            error = None,
            result = Some(CreateClaimResult(eisSuccessResponse.CaseID, Seq.empty))
          )
        )
      }

      "create-case request succeeds and file uploads succeed" in {
        when(mockClaimService.createClaim(any[EISCreateCaseRequest], anyString())(any())).thenReturn(
          Future.successful(eisSuccessResponse)
        )

        val uploadedFiles = Seq(UploadedFile("upscanReference", "downloadURL", "checksum", "fileName", "mimeType"))
        val fileTransferResults = Seq(FileTransferResult(
          upscanReference = "upscanReference",
          success = true,
          httpStatus = 202,
          transferredAt = ZonedDateTime.now.toLocalDateTime
        ))

        when(mockFileTransferService.transfer(eisSuccessResponse.CaseID, uploadedFiles)).thenReturn(
          Future.successful(fileTransferResults)
        )

        val request = claimRequest.copy(uploads = uploadedFiles)

        val result: Future[Result] =
          route(app, post.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(request))).get

        status(result) must be(CREATED)
        contentAsJson(result) mustBe toJson(
          CreateClaimResponse(
            correlationId = "xyz",
            error = None,
            result = Some(CreateClaimResult(eisSuccessResponse.CaseID, fileTransferResults))
          )
        )
      }
    }

    "return 400" when {

      "request fails" in {
        when(mockClaimService.createClaim(any[EISCreateCaseRequest], anyString())(any())).thenReturn(
          Future.successful(eisFailResponse)
        )
        val result: Future[Result] =
          route(app, post.withHeaders(("x-correlation-id", "xyz")).withJsonBody(toJson(claimRequest))).get

        status(result) must be(BAD_REQUEST)
        contentAsJson(result) mustBe toJson(
          CreateClaimResponse(
            correlationId = "xyz",
            error = Some(ApiError(eisFailResponse.errorCode.get, eisFailResponse.errorMessage)),
            result = None
          )
        )
      }

      "request is invalid" in {
        val result: Future[Result] = route(app, post.withJsonBody(Json.obj("field" -> "value"))).get

        status(result) must be(BAD_REQUEST)
        verifyNoInteractions(mockClaimService)
      }
    }
  }
}
