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

import javax.inject.Inject
import play.api.libs.json.{JsString, JsValue}
import uk.gov.hmrc.http.{ForbiddenException, HeaderCarrier}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.CreateCaseConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.EISCreateCaseResponse

import scala.concurrent.Future

class CreateCaseService @Inject() (createCaseConnector: CreateCaseConnector) {

  def submitClaim(eoriNumber: String, request: JsValue, correlationId: String)(implicit
    hc: HeaderCarrier
  ): Future[EISCreateCaseResponse] = {

    def eoriMatch(eoriContent: Option[JsValue]): Boolean = eoriContent.contains(JsString(eoriNumber))

    val eoriOk = (request \ "Content" \ "RepresentationType").toOption match {
      case Some(JsString("Importer")) => eoriMatch((request \ "Content" \ "ImporterDetails" \ "EORI").toOption)
      case Some(JsString("Representative of importer")) =>
        eoriMatch((request \ "Content" \ "AgentDetails" \ "EORI").toOption)
      case _ => false
    }

    if (eoriOk)
      createCaseConnector.submitClaim(request, correlationId)
    else
      Future.failed(new ForbiddenException("Bad user"))
  }

}
