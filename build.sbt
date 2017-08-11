
name := "Pact-Demo"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

publishArtifact := false

val versions = new {
  val scala = "2.11.8"
  //  val scala = "2.12.1"

  val akka = "2.5.3"
  val finatra = "2.11.0"
  val finatraLogging = "2.1.6"
  val javapact = "3.5.1"
  val json4s = "3.5.2"
  val junit = "4.12"
  val guice = "4.0"
  val logback = "1.2.3"
  val mockito = "1.10.19"
  val scalapact = "2.1.3"
  val scalatest = "3.0.1"
  val mustache = "0.9.5"
  val sl4j = "1.7.25"
}
lazy val commonSettings = Seq(
  version := "1.0",
  organization := "org.validoc",
  publishMavenStyle := true,
  scalaVersion := versions.scala,
  scalacOptions ++= Seq("-feature"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
  parallelExecution in Test := false,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    "Twitter Maven" at "https://maven.twttr.com",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"
  ),
  scriptClasspath := Seq("*"), // workaround for windows. https://github.com/sbt/sbt-native-packager/issues/72
  {
    for {
      username <- Option(System.getenv().get("SONATYPE_USERNAME"))
      password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
    } yield
      credentials += Credentials(
        "Sonatype Nexus Repository Manager",
        "oss.sonatype.org",
        username,
        password)
  }.getOrElse(credentials ++= Seq()),
  libraryDependencies += "org.mockito" % "mockito-all" % versions.mockito % "test",
  libraryDependencies += "org.scalatest" %% "scalatest" % versions.scalatest % "test",

  pomIncludeRepository := { _ => false },
  publishMavenStyle := true,
  publishArtifact in Test := false,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { _ => false },
  pomExtra in ThisBuild := (
    <url>https://github.com/phil-rice/PactDemo</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://www.opensource.org/licenses/bsd-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>https://github.com/phil-rice/PactDemo.git</url>
        <connection>https://github.com/phil-rice/PactDemo.git</connection>
      </scm>
      <developers>
        <developer>
          <id>phil.rice</id>
          <name>Phil Rice</name>
          <url>www.validoc.org</url>
        </developer>
      </developers>),
  publishArtifact in Test := false
)

lazy val logbackSettings = Seq(
  libraryDependencies += "ch.qos.logback" % "logback-classic" % versions.logback
)


lazy val finatraSettings = commonSettings ++ Seq(
  libraryDependencies += "com.twitter" %% "finatra-http" % versions.finatra,
  libraryDependencies += "com.twitter" %% "finatra-http" % versions.finatra % "test",
  libraryDependencies += "com.twitter" %% "inject-server" % versions.finatra % "test",
  libraryDependencies += "com.twitter" %% "inject-app" % versions.finatra % "test",
  libraryDependencies += "com.twitter" %% "inject-core" % versions.finatra % "test",
  libraryDependencies += "com.twitter" %% "inject-modules" % versions.finatra % "test",
  libraryDependencies += "com.google.inject.extensions" % "guice-testlib" % versions.guice % "test",
  libraryDependencies += "com.twitter" %% "finatra-jackson" % versions.finatra % "test",
  libraryDependencies += "junit" % "junit" % versions.junit % "test",

  libraryDependencies += "com.twitter" %% "finatra-http" % versions.finatra % "test" classifier "tests",
  libraryDependencies += "com.twitter" %% "inject-server" % versions.finatra % "test" classifier "tests",
  libraryDependencies += "com.twitter" %% "inject-app" % versions.finatra % "test" classifier "tests",
  libraryDependencies += "com.twitter" %% "inject-core" % versions.finatra % "test" classifier "tests",
  libraryDependencies += "com.twitter" %% "inject-modules" % versions.finatra % "test" classifier "tests",
  libraryDependencies += "com.google.inject.extensions" % "guice-testlib" % versions.guice % "test" classifier "tests",
  libraryDependencies += "com.twitter" %% "finatra-jackson" % versions.finatra % "test" classifier "tests"
)

lazy val jsonSettings = Seq(
  libraryDependencies += "org.json4s" % "json4s-native_2.11" % versions.json4s
)

lazy val mustacheSettings = Seq(
  libraryDependencies += "com.github.spullara.mustache.java" % "scala-extensions-2.11" % versions.mustache
)

lazy val pactConsumerSettings = finatraSettings ++ Seq(
  libraryDependencies += "com.itv" %% "scalapact-scalatest" % versions.scalapact % "test"
)

lazy val akkaSettings = Seq(
  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % versions.akka,
  libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % versions.akka % "test"
)

lazy val javaPactSettings = Seq(
  libraryDependencies += "au.com.dius" % "pact-jvm-consumer-junit_2.11" % versions.javapact % "test",
  libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
  crossPaths := false
)

lazy val utilities = (project in file("modules/utilities")).
  settings(commonSettings: _*)

lazy val finatra = (project in file("modules/finatra")).
  dependsOn(utilities % "test->test;compile->compile").aggregate(utilities).
  //  settings(log4JSettingsForTest: _*).
  settings(jsonSettings: _*).
  settings(finatraSettings: _*)

lazy val javaAndroidApp = (project in file("services/javaAndroidApp")).
  dependsOn(utilities, finatra).
  aggregate(utilities, finatra).
  settings(finatraSettings: _*).
  settings(javaPactSettings: _*).
  enablePlugins(JavaAppPackaging)

lazy val androidApp = (project in file("services/androidApp")).
  dependsOn(utilities % "test->test;compile->compile", finatra % "test->test;compile->compile", mustache).
  aggregate(utilities, finatra, mustache).
  settings(logbackSettings: _*).
  settings(pactConsumerSettings: _*).enablePlugins(JavaAppPackaging)

lazy val actorApp = (project in file("services/actorApp")).
  dependsOn(utilities % "test->test;compile->compile", finatra).
  aggregate(utilities, finatra).
  settings(pactConsumerSettings: _*).
  settings(akkaSettings: _*).
  enablePlugins(JavaAppPackaging)

lazy val iosApp = (project in file("services/iosApp")).
  dependsOn(utilities % "test->test;compile->compile", finatra % "test->test;compile->compile", mustache).
  aggregate(utilities, finatra).
  settings(pactConsumerSettings: _*).enablePlugins(JavaAppPackaging)

lazy val angularIOApp = (project in file("services/angularIOApp")).dependsOn(utilities, finatra).aggregate(utilities, finatra).
  settings(pactConsumerSettings: _*).enablePlugins(JavaAppPackaging)

lazy val provider = (project in file("services/provider")).
  dependsOn(utilities % "test->test;compile->compile", finatra % "test->test;compile->compile", mustache).
  aggregate(utilities, finatra, mustache).
  settings(finatraSettings: _*).
  settings(pactConsumerSettings: _*).
  enablePlugins(JavaAppPackaging)


lazy val mustache = (project in file("modules/mustache")).
  dependsOn(utilities % "test->test;compile->compile", finatra).
  aggregate(utilities, finatra).
  settings(commonSettings: _*).
  settings(mustacheSettings: _*)

lazy val globalTests = (project in file("modules/globalTests")).
  dependsOn(utilities % "test->test;compile->compile", finatra % "test->test;compile->compile").
  aggregate(utilities, finatra).
  settings(logbackSettings: _*).
  settings(commonSettings: _*)
