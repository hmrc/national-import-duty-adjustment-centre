import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "5.1.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % "5.1.0"   % Test,
    "org.scalatest"          %% "scalatest"              % "3.2.3"   % Test,
    "com.typesafe.play"      %% "play-test"              % current   % Test,
    "org.scalatestplus"      %% "mockito-3-4"            % "3.2.3.0" % Test,
    "com.vladsch.flexmark"    % "flexmark-all"           % "0.36.8"  % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "5.1.0"   % "test, it",
    "com.github.tomakehurst" %  "wiremock"               % "1.58"    % "it"
  )

}
