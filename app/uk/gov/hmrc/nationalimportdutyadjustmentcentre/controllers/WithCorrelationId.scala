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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.controllers

import play.api.mvc.{Request, Result, _}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.controllers.Responses.missingXCorrelationIdResponse

import scala.concurrent.Future

trait WithCorrelationId { self: Results =>

  protected def withCorrelationId(f: String => Future[Result])(implicit request: Request[_]): Future[Result] =
    request.headers.get("x-correlation-id") match {
      case Some(value) => f(value)
      case None        => Future.successful(missingXCorrelationIdResponse)
    }

}
