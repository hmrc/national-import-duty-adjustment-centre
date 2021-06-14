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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.support

import play.api.inject.guice.GuiceApplicationBuilder

trait TestApplication {
  _: BaseISpec =>

  def defaultAppBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.auth.port"                    -> wireMockPort,
        "microservice.services.eis.host"                           -> wireMockHost,
        "microservice.services.eis.port"                           -> wireMockPort,
        "microservice.services.trader-services.file-transfer.port" -> wireMockPort,
        "microservice.services.eis.createcaseapi.token"            -> "dummy-it-token",
        "microservice.services.eis.updatecaseapi.token"            -> "dummy-it-token",
        "metrics.enabled"                                          -> false,
        "auditing.enabled"                                         -> false,
        "auditing.consumer.baseUri.host"                               -> wireMockHost,
        "auditing.consumer.baseUri.port"                               -> wireMockPort,
        "microservice.services.trader-services.file-transfer.host"     -> wireMockHost,
        "microservice.services.trader-services.file-transfer.port"     -> wireMockPort,
        "microservice.services.trader-services.file-transfer.path"     -> "/file-transmission-synchronous-stub/transfer-file"
      )

}
