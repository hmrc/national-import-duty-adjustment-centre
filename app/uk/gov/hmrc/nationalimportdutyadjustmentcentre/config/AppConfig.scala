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

  val eisCreateCaseApiPath: String            = servicesConfig("eis.createcaseapi.path")
  val eisCreateCaseAuthorizationToken: String = servicesConfig("eis.createcaseapi.token")

  val eisUpdateCaseApiPath: String            = servicesConfig("eis.updatecaseapi.path")
  val eisUpdateCaseAuthorizationToken: String = servicesConfig("eis.updatecaseapi.token")

  val eisEnvironment: String = servicesConfig("eis.environment")

  val stubPegaCaseRef: () => String = () =>
    config
      .getOptional[String]("testonly.stub.ref")
      .getOrElse(throwConfigNotFoundError("testonly.stub.ref"))

  val fileTransferBaseUrl: String = servicesConfig.baseUrl("trader-services.file-transfer")

  val fileTransferPath: String = servicesConfig("trader-services.file-transfer.path")

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")

  val graphiteHost: String =
    config.get[String]("microservice.metrics.graphite.host")

  val eoriEnrolments: Seq[String] = config.get[Seq[String]]("eori.enrolments")

  private val allowListEnabled = config.get[Boolean]("eori.allowList.enabled")
  private val allowedEoris     = config.get[Seq[String]]("eori.allowList.eoris")

  def allowEori(eoriNumber: String): Boolean = !allowListEnabled || allowedEoris.contains(eoriNumber)

  private def servicesConfig(key: String): String = servicesConfig.getConfString(key, throwConfigNotFoundError(key))

  private def throwConfigNotFoundError(key: String) =
    throw new RuntimeException(s"Could not find config key '$key'")

}
