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

import akka.actor.ActorSystem
import com.google.inject.Inject
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, _}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.config.AppConfig
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{
  EISCreateCaseError,
  EISCreateCaseResponse,
  EISCreateCaseSuccess
}

import scala.concurrent.{ExecutionContext, Future}

class CreateCaseConnector @Inject() (val config: AppConfig, val http: HttpPost, val actorSystem: ActorSystem)(implicit
  ec: ExecutionContext
) extends ReadSuccessOrFailure[EISCreateCaseResponse, EISCreateCaseSuccess, EISCreateCaseError](
      EISCreateCaseError.fromStatusAndMessage
    ) with EISConnector with Retry {

  val url: String = config.eisBaseUrl + config.eisCreateCaseApiPath

  def submitClaim(request: JsValue, correlationId: String)(implicit hc: HeaderCarrier): Future[EISCreateCaseResponse] =
    retry(config.retryDurations: _*)(
      EISCreateCaseResponse.shouldRetry,
      EISCreateCaseResponse.errorMessage,
      EISCreateCaseResponse.delayInterval
    ) {
      http.POST[JsValue, EISCreateCaseResponse](
        url,
        request,
        eisApiHeaders(
          correlationId,
          config.eisEnvironment,
          config.eisCreateCaseAuthorizationToken
        ) ++ mdtpTracingHeaders(hc)
      )(
        implicitly[Writes[JsValue]],
        readFromJsonSuccessOrFailure,
        hc.copy(authorization = None),
        implicitly[ExecutionContext]
      )
    }

}
