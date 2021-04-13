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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis

import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.UnitSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.utils.TestData

import java.time.Instant

class EISCreateCaseResponseSpec extends UnitSpec with TestData {

  "EISCreateCaseResponse" when {
    "reads" when {
      "using a valid response" should {
        "return a valid EISCreateCaseSuccess" in {
          EISCreateCaseResponse.reads.reads(
            Json.parse(EISCreateCaseResponseSpec.successCreateResponse)
          ) mustBe JsSuccess(EISCreateCaseResponseSpec.createCaseSuccess)
        }
      }

      "using a error response" should {
        "return a valid EISCreateCaseError" in {
          EISCreateCaseResponse.reads.reads(Json.parse(EISCreateCaseResponseSpec.errorCreateResponse)) mustBe JsSuccess(
            EISCreateCaseResponseSpec.createCaseError
          )
        }
      }
    }
  }

  "writes" when {
    "giving an createCaseSuccess" should {
      "return a valid Json object" in {
        EISCreateCaseResponse.writes.writes(EISCreateCaseResponseSpec.createCaseSuccess) mustBe Json.parse(
          EISCreateCaseResponseSpec.successCreateResponse
        )
      }
    }
  }

  "writes" when {
    "giving an createCaseError" should {
      "return a valid Json object" in {
        EISCreateCaseResponse.writes.writes(EISCreateCaseResponseSpec.createCaseError) mustBe Json.parse(
          EISCreateCaseResponseSpec.errorCreateResponse
        )
      }
    }
  }
}

object EISCreateCaseResponseSpec {

  val createCaseSuccess = EISCreateCaseSuccess(
    "11370e18-6e24-453e-b45a-76d3e32ea33d",
    Instant.parse("2018-04-24T09:30:00Z"),
    "Created200",
    "Created Claim"
  )

  val createCaseError = EISCreateCaseError(
    EISErrorDetail(
      Some("PEGAERROR500"),
      Some("Pega error message 500"),
      Some("abcdefg-awe-errr-errr-errrooorrr"),
      Instant.parse("2018-04-24T09:30:00Z")
    )
  )

  val successCreateResponse: String =
    """
      |{
      |    "CaseID" : "11370e18-6e24-453e-b45a-76d3e32ea33d",
      |    "ProcessingDate": "2018-04-24T09:30:00Z",
      |    "Status" : "Created200",
      |    "StatusText": "Created Claim"
      |}
        """.stripMargin

  val errorCreateResponse: String =
    """
      |{
      |    "errorDetail" : {
      |        "processingDate": "2018-04-24T09:30:00Z",
      |        "correlationId" : "abcdefg-awe-errr-errr-errrooorrr",
      |        "errorCode" : "PEGAERROR500",
      |        "errorMessage": "Pega error message 500"
      |    }
      |}
        """.stripMargin

}
