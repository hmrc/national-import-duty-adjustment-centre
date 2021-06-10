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

import com.google.inject.Singleton
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{CreateEISClaimRequest, FileTransferAudit}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class AuditService @Inject() (val auditConnector: AuditConnector) {

  import AuditService._

  final def auditCreateCaseEvent(createRequest: CreateEISClaimRequest)
  (auditFileTransfers: FileTransferAudit)
                                (implicit hc: HeaderCarrier, request: Request[Any], ec: ExecutionContext): Future[Unit] = {
    val details: JsValue =
      FileTransferAuditEventDetails.from(auditFileTransfers)
    auditExtendedEvent("CreateCaseFileTransfer", "create-case-file-transfer", details)
  }

  private def auditExtendedEvent(
                                  event: String,
                                  transactionName: String,
                                  details: JsValue
                                )(implicit hc: HeaderCarrier, request: Request[Any], ec: ExecutionContext): Future[Unit] =
    sendExtended(createExtendedEvent(event, transactionName, details))

  private def createExtendedEvent(
                                   event: String,
                                   transactionName: String,
                                   details: JsValue
                                 )(implicit hc: HeaderCarrier, request: Request[Any], ec: ExecutionContext): ExtendedDataEvent = {
    val tags = hc.toAuditTags(transactionName, request.path)
    ExtendedDataEvent(
      auditSource = "national-import-duty-adjustment-centre",
      auditType = event,
      tags = tags,
      detail = details
    )
  }


  private def sendExtended(
    events: ExtendedDataEvent*
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    Future {
      events.foreach { event =>
        Try(auditConnector.sendExtendedEvent(event))
      }
    }
}

object AuditService {

  object FileTransferAuditEventDetails {

    def from(fileTransferAudit: FileTransferAudit): JsValue = {
      val payload: JsValue = Json.toJson(fileTransferAudit)
      println("******************************************************")
      println("******************************************************")
      println(s"audit json is ${payload}")
      println("******************************************************")
      println("******************************************************")
      val auditJson: JsObject = payload.as[JsObject]
      auditJson
    }
  }

}
