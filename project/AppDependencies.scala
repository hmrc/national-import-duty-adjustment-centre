import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "5.3.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % "5.3.0"   % Test,
    "org.scalatest"          %% "scalatest"              % "3.2.9"   % Test,
    "com.typesafe.play"      %% "play-test"              % current   % Test,
    "com.typesafe.akka"      %% "akka-testkit"           % "2.6.10" % Test,
    "org.scalatestplus"      %% "mockito-3-4"            % "3.2.9.0" % Test,
    "com.vladsch.flexmark"    % "flexmark-all"           % "0.36.8"  % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "5.1.0"   % "test, it",
    "com.github.tomakehurst" %  "wiremock-jre8"          % "2.26.3"  % "it"
  )

}
