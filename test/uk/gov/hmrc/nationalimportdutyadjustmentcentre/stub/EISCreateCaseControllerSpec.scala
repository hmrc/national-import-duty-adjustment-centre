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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.stub

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json.toJson
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.ControllerSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.MicroserviceAuthConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.utils.TestData

import scala.concurrent.Future

class EISCreateCaseControllerSpec extends ControllerSpec with GuiceOneAppPerSuite with TestData {
  val configuredRef: String = "CONFIGURED_REF"

  override lazy val app: Application = GuiceApplicationBuilder()
    .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
    .configure("testonly.stub.ref" -> configuredRef)
    .overrides(bind[MicroserviceAuthConnector].to(mockAuthConnector))
    .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    withAuthorizedUser()
  }

  "EIS stub create-case" should {

    val post = FakeRequest("POST", "/eis-stub/create-case")

    "return 200 with Json payload containing configured case reference number" in {
      val result: Future[Result] =
        route(app, post.withJsonBody(toJson(eisCreateCaseRequest(claimRequest)))).get

      status(result) must be(OK)
      (contentAsJson(result) \ "CaseID").as[String] must be(configuredRef)
    }
  }
}
