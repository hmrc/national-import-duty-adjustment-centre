package uk.gov.hmrc.nationalimportdutyadjustmentcentre.services

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.UnitSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.UploadedFile

class FileTransferServiceSpec extends UnitSpec with ScalaFutures {
  val service: FileTransferService = new FileTransferService()
  private val uploadedFile1 = UploadedFile("upscanReference1", "downloadURL1", "checksum1", "fileName1", "mimeType1")
  private val uploadedFile2 = UploadedFile("upscanReference2", "downloadURL2", "checksum1", "fileName2", "mimeType2")

  "FileTransferService" should {
    "return a successful FileTransferResult per uploaded file" when {
      "transferFile called" in {

        val uploadedFiles = Seq(uploadedFile1, uploadedFile2)
        val transferResults = service.transfer(uploadedFiles).futureValue

        transferResults.length must be(uploadedFiles.length)
        transferResults.head.upscanReference must be (uploadedFile1.upscanReference)
        transferResults(1).upscanReference must be (uploadedFile2.upscanReference)

      }
    }
  }
}
