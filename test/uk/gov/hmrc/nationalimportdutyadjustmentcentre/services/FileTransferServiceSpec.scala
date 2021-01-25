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
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.UnitSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.FileTransferConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.FileTransferResult
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.utils.TestData

import scala.concurrent.{ExecutionContext, Future}

class FileTransferServiceSpec extends UnitSpec with ScalaFutures with TestData {

  implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockConnector: FileTransferConnector = mock[FileTransferConnector]
  val service: FileTransferService         = new FileTransferService(mockConnector)

  val mockFileTransferResult: FileTransferResult = mock[FileTransferResult]

  "FileTransferService" should {
    "return a successful FileTransferResult per uploaded file" when {
      "transferFile called" in {

        when(mockConnector.transferFile(any(), any())(any())).thenReturn(Future.successful(mockFileTransferResult))

        val uploads         = uploadedFiles("upscanReference1", "upscanReference2")
        val transferResults = service.transferFiles("caseReferenceNumber", "conversationId", uploads).futureValue

        transferResults.length must be(uploads.length)
      }
    }
  }
}
