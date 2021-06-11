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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.actors

import akka.actor.{Actor, ActorRef, Status}
import akka.pattern.pipe
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.actors.FileTransferAuditActor.AuditFileTransferResults
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.FileTransferConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.TraderServicesFileTransferRequest
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{FileTransferResult, UploadedFile}

import java.time.{LocalDateTime, ZoneOffset}
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class FileTransferActor(
                         caseReferenceNumber: String,
                         fileTransferConnector: FileTransferConnector,
                         conversationId: String,
                         auditor: ActorRef
                       ) extends Actor {

  import FileTransferActor._
  import context.dispatcher

  var clientRef: ActorRef = ActorRef.noSender
  var results: Seq[FileTransferResult] = Seq.empty

  override def receive: Receive = {


    case TransferMultipleFiles(files, batchSize, headerCarrier) =>
      clientRef = sender()
      files.map {
        case (file, index) => TransferSingleFile(file, index, batchSize, headerCarrier)
      }
      .foreach(message => self ! message)
      self ! CheckComplete(batchSize)

    case TransferSingleFile(file, index, batchSize, headerCarrier) =>
      transferAFile(file, index, batchSize)(headerCarrier)
        .pipeTo(sender())

    case result: FileTransferResult =>
      results = results :+ result

    case Status.Failure(error @ UpstreamErrorResponse(message, code, _, _)) =>
      Logger(getClass).error(error.toString)
      results = results :+ FileTransferResult(
        upscanReference = "<unknown>",
        success = false,
        httpStatus = code,
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        error = Some(message)
      )

    case akka.actor.Status.Failure(error) =>
      Logger(getClass).error(error.toString())
      results = results :+ FileTransferResult(
        upscanReference = "<unknown>",
        success = false,
        httpStatus = 0,
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        error = Some(error.toString())
      )

    case CheckComplete(batchSize) =>

      if (results.size == batchSize) {

        auditor ! AuditFileTransferResults(results)

        context.stop(self)
      } else
        context.system.scheduler
          .scheduleOnce(FiniteDuration(500, "ms"), self, CheckComplete(batchSize))
  }

  def transferAFile(file: UploadedFile, index: Int, batchSize: Int)(implicit hc: HeaderCarrier): Future[FileTransferResult] =
    fileTransferConnector.transferFile(TraderServicesFileTransferRequest
      .fromUploadedFile(
        caseReferenceNumber,
        conversationId,
        applicationName = "NIDAC",
        batchSize = batchSize,
        batchCount = index + 1,
        uploadedFile = file
      ))
}

object FileTransferActor {
  case class TransferMultipleFiles(files: Seq[(UploadedFile, Int)], batchSize: Int, hc: HeaderCarrier)
  case class TransferSingleFile(file: UploadedFile, index: Int, batchSize: Int, hc: HeaderCarrier)
  case class CheckComplete(batchSize: Int)
}
