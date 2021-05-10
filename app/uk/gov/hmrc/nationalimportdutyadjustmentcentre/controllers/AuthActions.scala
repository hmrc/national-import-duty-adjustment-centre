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

import play.api.mvc.Result
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.controllers.Responses.invalidUserResponse

import scala.concurrent.{ExecutionContext, Future}

trait AuthActions extends AuthorisedFunctions {

  protected def withAuthorised[A](
    body: => Future[Result]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Result] =
    authorised(AuthProviders(GovernmentGateway) and Organisation)(body) recover {
      case _: AuthorisationException => invalidUserResponse
    }

}
