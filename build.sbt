
name := "Pact-Demo"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

publishArtifact := false

val versions = new {
  val scala = "2.11.8"
  //  val scala = "2.12.1"
  val finatra = "2.11.0"
  val finatraLogging = "2.1.6"
  val json4s = "3.5.2"
  val junit = "4.12"
  val guice = "4.0"
  val mockito = "1.10.19"
  val scalatest = "3.0.1"
  val scalapact = "2.1.3"
  val akka = "2.5.3"
}
lazy val commonSettings = Seq(
  version := "1.0",
  organization := "org.validoc",
  publishMavenStyle := true,
  scalaVersion := versions.scala,
  scalacOptions ++= Seq("-feature"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
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

lazy val pactConsumerSettings = finatraSettings ++ Seq(
  libraryDependencies += "com.itv" %% "scalapact-scalatest" % versions.scalapact
)

lazy val akkaSettings = Seq(
  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % versions.akka
)

lazy val appUtilities = (project in file("PactDemoSharedCode/modules/utilities")).
  settings(finatraSettings: _*).
  settings(jsonSettings: _*).
  settings(akkaSettings: _*)

lazy val finatraUtilities = (project in file("PactDemoSharedCode/modules/finatra")).dependsOn(appUtilities).aggregate(appUtilities).
  settings(finatraSettings: _*)


lazy val androidApp = (project in file("PactDemoAndroidApp")).dependsOn(appUtilities, finatraUtilities).aggregate(appUtilities, finatraUtilities).
  settings(pactConsumerSettings: _*).enablePlugins(JavaAppPackaging)

lazy val akkaApp = (project in file("PactDemoAkkaApp")).dependsOn(appUtilities, finatraUtilities).aggregate(appUtilities, finatraUtilities).
  settings(pactConsumerSettings: _*).enablePlugins(JavaAppPackaging)

lazy val iosApp = (project in file("PactDemoIosApp")).dependsOn(appUtilities, finatraUtilities).aggregate(appUtilities, finatraUtilities).
  settings(pactConsumerSettings: _*).enablePlugins(JavaAppPackaging)

lazy val angularIOApp = (project in file("PactDemoAngularIOApp")).dependsOn(appUtilities, finatraUtilities).aggregate(appUtilities, finatraUtilities).
  settings(pactConsumerSettings: _*).enablePlugins(JavaAppPackaging)
  
lazy val provider = (project in file("PactDemoProvider")).
  dependsOn(appUtilities % "test->test;compile->compile", finatraUtilities).aggregate(appUtilities, finatraUtilities).
  settings(finatraSettings: _*).
  settings(pactConsumerSettings: _*).
  enablePlugins(JavaAppPackaging)