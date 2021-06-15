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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpPost, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.UnitSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.config.AppConfig
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.FileTransferResult
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.FileTransferRequest

import scala.concurrent.{ExecutionContext, Future}

class FileTransferConnectorSpec extends UnitSpec with ScalaFutures with BeforeAndAfterEach {

  implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockHttpPost: HttpPost   = mock[HttpPost]
  val mockAppConfig: AppConfig = mock[AppConfig]

  val request  = mock[FileTransferRequest]
  val response = mock[HttpResponse]

  val connector = new FileTransferConnector(mockAppConfig, mockHttpPost)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockAppConfig.fileTransferBaseUrl).thenReturn("base")
    when(mockAppConfig.fileTransferPath).thenReturn("/path")
  }

  override protected def afterEach(): Unit = {
    reset(mockHttpPost, mockAppConfig, request)
    super.afterEach()
  }

  "FileTransferConnector" should {
    def httpOnPost =
      when(mockHttpPost.POST[FileTransferRequest, HttpResponse](any(), any(), any())(any(), any(), any(), any()))

    "return a successful FileTransferResult" in {
      httpOnPost.thenReturn(Future.successful(HttpResponse(200, "Body")))

      val transferResults: FileTransferResult = connector.transferFile(request).futureValue

      transferResults.success must be(true)
    }

    "return an unsuccessful FileTransferResult" in {
      httpOnPost.thenReturn(Future.successful(HttpResponse(301, "Body")))

      val transferResults: FileTransferResult = connector.transferFile(request).futureValue

      transferResults.success must be(false)
      transferResults.httpStatus must be(301)
    }

    "handle any other exception" in {
      httpOnPost.thenAnswer((_) => Future.failed(new Exception("Something went wrong")))

      val transferResults: FileTransferResult = connector.transferFile(request).futureValue

      transferResults.success must be(false)
      transferResults.httpStatus must be(500)
      transferResults.error must be(Some("Something went wrong"))
    }
  }
}
