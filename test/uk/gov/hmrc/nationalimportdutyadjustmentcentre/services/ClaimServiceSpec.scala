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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.services

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.base.UnitSpec
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{CreateClaimRequest, CreateClaimResponse}

class ClaimServiceSpec extends UnitSpec with ScalaFutures {

  val service: ClaimService = new ClaimService

  "ClaimService" should {

    "return response" when {

      "create called" in {

        val request                       = CreateClaimRequest("user-id", "claimType")
        val response: CreateClaimResponse = service.create(request).futureValue

        response.userId mustBe request.userId
        response.claimReference must not be empty
      }
    }
  }
}
