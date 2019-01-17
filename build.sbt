enablePlugins(GatlingPlugin, FrontLinePlugin)

scalaVersion := "2.12.8"
scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps"
)

val gatlingVersion = "3.0.2"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test"
libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion % "test"
