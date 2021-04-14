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

class EISUpdateCaseResponseSpec extends UnitSpec with TestData {

  "EISUpdateCaseResponse" when {
    "reads" when {
      "using a valid response" should {
        "return a valid EISUpdateCaseSuccess" in {
          EISUpdateCaseResponse.reads.reads(
            Json.parse(EISUpdateCaseResponseSpec.successUpdateResponse)
          ) mustBe JsSuccess(EISUpdateCaseResponseSpec.updateCaseSuccess)
        }
      }

      "using a error response" should {
        "return a valid EISUpdateCaseError" in {
          EISUpdateCaseResponse.reads.reads(Json.parse(EISUpdateCaseResponseSpec.errorUpdateResponse)) mustBe JsSuccess(
            EISUpdateCaseResponseSpec.updateCaseError
          )
        }
      }
    }
  }

  "writes" when {
    "giving an updateCaseSuccess" should {
      "return a valid Json object" in {
        EISUpdateCaseResponse.writes.writes(EISUpdateCaseResponseSpec.updateCaseSuccess) mustBe Json.parse(
          EISUpdateCaseResponseSpec.successUpdateResponse
        )
      }
    }
  }

  "writes" when {
    "giving an updateCaseError" should {
      "return a valid Json object" in {
        EISUpdateCaseResponse.writes.writes(EISUpdateCaseResponseSpec.updateCaseError) mustBe Json.parse(
          EISUpdateCaseResponseSpec.errorUpdateResponse
        )
      }
    }
  }

}

object EISUpdateCaseResponseSpec {

  val updateCaseSuccess = EISUpdateCaseSuccess(
    "eeeeef-6e24-453e-b45a-76d3e32ea33d",
    Instant.parse("2018-04-24T09:30:00Z"),
    "Updated200",
    "Updated Claim"
  )

  val updateCaseError = EISUpdateCaseError(
    EISErrorDetail(
      Some("PEGAERROR500"),
      Some("Pega error message 500"),
      Some("abcdefg-awe-errr-errr-errrooorrr"),
      Instant.parse("2018-04-24T09:30:00Z")
    )
  )

  val successUpdateResponse: String =
    """
      |{
      |    "CaseID" : "eeeeef-6e24-453e-b45a-76d3e32ea33d",
      |    "ProcessingDate": "2018-04-24T09:30:00Z",
      |    "Status" : "Updated200",
      |    "StatusText": "Updated Claim"
      |}
        """.stripMargin

  val errorUpdateResponse: String =
    """
      |{
      |    "errorDetail" : {
      |        "timestamp": "2018-04-24T09:30:00Z",
      |        "correlationId" : "abcdefg-awe-errr-errr-errrooorrr",
      |        "errorCode" : "PEGAERROR500",
      |        "errorMessage": "Pega error message 500"
      |    }
      |}
        """.stripMargin

}
