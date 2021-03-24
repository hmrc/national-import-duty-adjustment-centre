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

import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.FileTransferConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.TraderServicesFileTransferRequest
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{FileTransferResult, UploadedFile}

import scala.concurrent.{ExecutionContext, Future}

class FileTransferService @Inject() (fileTransferConnector: FileTransferConnector)(implicit ec: ExecutionContext) {

  def transferFiles(caseReferenceNumber: String, conversationId: String, uploadedFiles: Seq[UploadedFile])(implicit
    hc: HeaderCarrier
  ): Future[Seq[FileTransferResult]] =
    Future.sequence(
      uploadedFiles.zipWithIndex
        .map {
          case (file, index) =>
            TraderServicesFileTransferRequest
              .fromUploadedFile(
                caseReferenceNumber,
                conversationId,
                applicationName = "NIDAC",
                batchSize = uploadedFiles.size,
                batchCount = index + 1,
                uploadedFile = file
              )
        }
        .map(fileTransferConnector.transferFile(_))
    )

}
