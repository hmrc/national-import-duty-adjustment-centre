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

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.UnitSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.UploadedFile

class FileTransferServiceSpec extends UnitSpec with ScalaFutures {
  val service: FileTransferService = new FileTransferService()
  private val uploadedFile1        = UploadedFile("upscanReference1", "downloadURL1", "checksum1", "fileName1", "mimeType1")
  private val uploadedFile2        = UploadedFile("upscanReference2", "downloadURL2", "checksum1", "fileName2", "mimeType2")

  "FileTransferService" should {
    "return a successful FileTransferResult per uploaded file" when {
      "transferFile called" in {

        val uploadedFiles   = Seq(uploadedFile1, uploadedFile2)
        val transferResults = service.transfer("caseReferenceNumber", uploadedFiles).futureValue

        transferResults.length must be(uploadedFiles.length)
        transferResults.head.upscanReference must be(uploadedFile1.upscanReference)
        transferResults(1).upscanReference must be(uploadedFile2.upscanReference)

      }
    }
  }
}
