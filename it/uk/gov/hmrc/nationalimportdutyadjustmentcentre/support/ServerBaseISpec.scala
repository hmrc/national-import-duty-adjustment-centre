package uk.gov.hmrc.nationalimportdutyadjustmentcentre.support

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

abstract class ServerBaseISpec extends BaseISpec with GuiceOneServerPerSuite with TestApplication with ScalaFutures {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(4, Seconds), interval = Span(1, Seconds))

}
