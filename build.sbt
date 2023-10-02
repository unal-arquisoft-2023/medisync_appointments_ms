// val Http4sVersion = "1.0.0-M29"
val Http4sVersion = "0.23.23"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.6"
val MunitCatsEffectVersion = "1.0.6"
val PureconfigVersion = "0.17.4"
val DoobieVersion = "1.0.0-RC4"
val FlywayVersion = "9.22.2"
val MonixNewtypesVersion = "0.2.3"
val CirceVersion = "0.14.5"
val SpireVersion = "0.18.0"
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.first   
  case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
}
// ThisBuild / assemblyShadeRules := Seq(
//   ShadeRule.rename("com.fasterxml.core.**" -> "shadeio.@1").inAll
// )

lazy val root = (project in file("."))
  .settings(
    organization := "com.medisync",
    name := "quickstart",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.0",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"      %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "org.scalameta"   %% "munit"               % MunitVersion           % Test,
      "org.typelevel"   %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "com.github.pureconfig" %% "pureconfig-core" % PureconfigVersion, 
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-http4s" % PureconfigVersion,

      "org.tpolecat"%% "doobie-core" % DoobieVersion,
      "org.tpolecat"%% "doobie-postgres" % DoobieVersion,
      "org.tpolecat"%% "doobie-specs2" % DoobieVersion,
      "org.tpolecat"%% "doobie-hikari" % DoobieVersion,

      "io.circe" %% "circe-core" % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-literal" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,

      "org.flywaydb" % "flyway-core" % FlywayVersion,

      "io.monix" %% "newtypes-core" % MonixNewtypesVersion,
      "io.monix" %% "newtypes-circe-v0-14" % MonixNewtypesVersion,

      "org.typelevel" %% "spire" % SpireVersion,
      "org.typelevel" %% "spire-extras" % SpireVersion,

    ),
    testFrameworks += new TestFramework("munit.Framework")
  )