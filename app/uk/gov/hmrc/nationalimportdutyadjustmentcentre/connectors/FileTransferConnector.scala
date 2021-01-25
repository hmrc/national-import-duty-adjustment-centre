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

import javax.inject.Inject
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.config.AppConfig
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.FileTransferResult
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.TraderServicesFileTransferRequest

import scala.concurrent.{ExecutionContext, Future}

class FileTransferConnector @Inject() (val config: AppConfig, val http: HttpPost)(implicit ec: ExecutionContext) {

  final lazy val url = config.fileTransferBaseUrl + config.fileTransferPath

  def transferFile(
    fileTransferRequest: TraderServicesFileTransferRequest
  )(implicit hc: HeaderCarrier): Future[FileTransferResult] =
    http
      .POST[TraderServicesFileTransferRequest, FileTransferResult](url, fileTransferRequest)

}
