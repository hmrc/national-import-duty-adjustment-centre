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
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{EISUpdateCaseError, EISUpdateCaseResponse, EISUpdateCaseSuccess}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class UpdateCaseConnector @Inject() (val config: AppConfig, val http: HttpPost, val actorSystem: ActorSystem)(implicit ec: ExecutionContext)
    extends ReadSuccessOrFailure[EISUpdateCaseResponse, EISUpdateCaseSuccess, EISUpdateCaseError](
      EISUpdateCaseError.fromStatusAndMessage
    ) with EISConnector with Retry {

  val url: String = config.eisBaseUrl + config.eisUpdateCaseApiPath

  def updateClaim(request: JsValue, correlationId: String)(implicit hc: HeaderCarrier): Future[EISUpdateCaseResponse] =
    retry(FiniteDuration(1, TimeUnit.SECONDS), FiniteDuration(2, TimeUnit.SECONDS), FiniteDuration(3, TimeUnit.SECONDS), FiniteDuration(3, TimeUnit.SECONDS))(
      EISUpdateCaseResponse.shouldRetry,
      EISUpdateCaseResponse.errorMessage
    ) {
      val eventualResponse = http.POST[JsValue, EISUpdateCaseResponse](
        url,
        request,
        eisApiHeaders(correlationId, config.eisEnvironment, config.eisUpdateCaseAuthorizationToken)
      )(
        implicitly[Writes[JsValue]],
        readFromJsonSuccessOrFailure,
        hc.copy(authorization = None),
        implicitly[ExecutionContext]
      )
      eventualResponse
    }

}
