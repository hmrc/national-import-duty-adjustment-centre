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

import com.google.inject.Inject
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, _}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.config.AppConfig
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{
  EISCreateCaseError,
  EISCreateCaseResponse,
  EISCreateCaseSuccess,
  EISErrorDetail
}

import scala.concurrent.{ExecutionContext, Future}

class CreateCaseConnector @Inject() (val config: AppConfig, val http: HttpPost)(implicit ec: ExecutionContext)
    extends ReadSuccessOrFailure[EISCreateCaseResponse, EISCreateCaseSuccess, EISCreateCaseError](
      EISCreateCaseError.fromStatusAndMessage
    ) with EISConnector {

  val url: String = config.eisBaseUrl + config.eisCreateCaseApiPath

  def submitClaim(request: JsValue, correlationId: String)(implicit hc: HeaderCarrier): Future[EISCreateCaseResponse] =
    http.POST[JsValue, EISCreateCaseResponse](
      url,
      request,
      eisApiHeaders(correlationId, config.eisEnvironment, config.eisCreateCaseAuthorizationToken)
    )(
      implicitly[Writes[JsValue]],
      readFromJsonSuccessOrFailure,
      hc.copy(authorization = None),
      implicitly[ExecutionContext]
    ) recover {
      case error: UpstreamErrorResponse =>
        EISCreateCaseError(
          EISErrorDetail(errorCode = Some(s"ERROR${error.statusCode}"), errorMessage = Some(error.message))
        )
    }

}
