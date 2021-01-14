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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.base

import com.codahale.metrics.SharedMetricRegistries
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{AuthProviders, MissingBearerToken}
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.MicroserviceAuthConnector

import scala.concurrent.{ExecutionContext, Future}

trait ControllerSpec extends UnitSpec with Injector with MockitoSugar with BeforeAndAfterEach {

  SharedMetricRegistries.clear()

  implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val mockAuthConnector = mock[MicroserviceAuthConnector]

  def withAuthorizedUser(): Unit =
    when(
      mockAuthConnector.authorise(meq(AuthProviders(GovernmentGateway)), meq(EmptyRetrieval))(
        any[HeaderCarrier],
        any[ExecutionContext]
      )
    ).thenReturn(Future.successful(()))

  def withUnauthorizedUser(): Unit =
    when(mockAuthConnector.authorise(any(), any())(any(), any())).thenReturn(Future.failed(MissingBearerToken()))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(
      mockAuthConnector.authorise(meq(AuthProviders(GovernmentGateway)), meq(EmptyRetrieval))(
        any[HeaderCarrier],
        any[ExecutionContext]
      )
    ).thenReturn(Future.successful(()))
  }

  override protected def afterEach(): Unit = {
    reset(mockAuthConnector)
    super.afterEach()
  }

}
