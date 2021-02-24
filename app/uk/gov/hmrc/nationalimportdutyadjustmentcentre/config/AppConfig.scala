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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {

  val authBaseUrl: String = servicesConfig.baseUrl("auth")

  val eisBaseUrl: String = servicesConfig.baseUrl("eis")

  val eisCreateCaseApiPath: String =
    servicesConfig.getConfString("eis.createcaseapi.path", throwConfigNotFoundError("eis.createcaseapi.path"))

  val eisUpdateCaseApiPath: String =
    servicesConfig.getConfString("eis.updatecaseapi.path", throwConfigNotFoundError("eis.updatecaseapi.path"))

  val eisAuthorizationToken: String =
    servicesConfig.getConfString("eis.token", throwConfigNotFoundError("eis.token"))

  val eisEnvironment: String = servicesConfig.getConfString(
    "eis.environment",
    throwConfigNotFoundError("eis.environment")
  )

  val stubPegaCaseRef: () => String = () =>
    config
      .getOptional[String]("testonly.stub.ref")
      .getOrElse(throwConfigNotFoundError("testonly.stub.ref"))

  val fileTransferBaseUrl: String = servicesConfig.baseUrl("trader-services.file-transfer")

  val fileTransferPath: String =
    servicesConfig.getConfString(
      "trader-services.file-transfer.path",
      throwConfigNotFoundError("trader-services.file-transfer.path")
    )

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")

  val graphiteHost: String =
    config.get[String]("microservice.metrics.graphite.host")

  private def throwConfigNotFoundError(key: String) =
    throw new RuntimeException(s"Could not find config key '$key'")

}
