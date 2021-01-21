package uk.gov.hmrc.nationalimportdutyadjustmentcentre.services

import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{FileTransferResult, UploadedFile}

import java.time.ZonedDateTime
import scala.concurrent.Future

class FileTransferService {
  def transfer(uploads: Seq[UploadedFile]): Future[Seq[FileTransferResult]]= {
    Future.successful(
      uploads.map(upload => FileTransferResult(
        upscanReference = upload.upscanReference,
        success = true,
        httpStatus = 202,
        transferredAt = ZonedDateTime.now.toLocalDateTime
      ))
    )
  }
}
