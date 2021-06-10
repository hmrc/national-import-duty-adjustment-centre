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
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.FileTransferConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.TraderServicesFileTransferRequest
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{FileTransferAudit, FileTransferResult, UploadedFile}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.{LocalDateTime, ZoneOffset}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

class FileTransferAuditActor(
                              caseReferenceNumber: String,
                              auditConnector: AuditConnector,
                              conversationId: String
                       ) extends Actor {

  import context.dispatcher
  import FileTransferAuditActor.AuditFileTransferResults


  override def receive: Receive = {

    case AuditFileTransferResults(results, headerCarrier, ec) =>

      println(" We are in another actor")
      println(" We are in another actor")
      println(" We are in another actor")
      println(" We are in another actor")
      println(" We are in another actor")
      println(" We are in another actor")
      println(" We are in another actor")
      println(" We are in another actor")
      println(" We are in another actor")
      println(" ======================== ")
      println(s"${caseReferenceNumber} - ${conversationId}")

//      val body = FileTransferAudit(results)
//      auditConnector.sendExplicitAudit("pleaseAudit", body)(headerCarrier, ec, Json.writes[FileTransferAudit])


  }


}

object FileTransferAuditActor {
  case class AuditFileTransferResults(results: Seq[FileTransferResult], headerCarrier: HeaderCarrier, executionContext: ExecutionContext)
}


