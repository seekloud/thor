import sbt._

/**
  * User: Taoz
  * Date: 6/13/2017
  * Time: 9:38 PM
  */
object Dependencies {




  val slickV = "3.2.3"
  val slickPgV = "0.16.1"
  val akkaV = "2.5.13"
  val akkaHttpV = "10.1.1"

  val circeVersion = "0.9.3"

  val scalaJsDomV = "0.9.2"
  val scalaTagsV = "0.6.5"
  val diodeV = "1.1.2"


  val akkaSeq = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV withSources (),
    //"com.typesafe.akka" %% "akka-typed" % akkaV withSources (),
    "com.typesafe.akka" %% "akka-actor-typed" % akkaV withSources (),
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-stream-typed" % akkaV
  )

  val akkaHttpSeq = Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV
  )

  val circeSeq = Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion
  )

  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
  val slick = "com.typesafe.slick" %% "slick" % "3.2.1"
  val slickCodeGen = "com.typesafe.slick" %% "slick-codegen" % "3.2.1"
  val slickLess = Seq(
    "com.chuusai"        %% "shapeless" % "2.3.3",
    "io.underscore"      %% "slickless" % "0.3.3"
  )
  val slickPg = Seq(
    "com.github.tminglei" %% "slick-pg" % slickPgV,
    "com.github.tminglei" %% "slick-pg_circe-json" % slickPgV
  )

  val scalikejdbc = "org.scalikejdbc" %% "scalikejdbc" % "2.5.0"
  val scalikejdbcConfig = "org.scalikejdbc" %% "scalikejdbc-config" % "2.5.0"

  val scalatags = "com.lihaoyi" %% "scalatags" % "0.6.5"
  val nscalaTime = "com.github.nscala-time" %% "nscala-time" % "2.16.0"
  val hikariCP = "com.zaxxer" % "HikariCP" % "2.6.2"
  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val codec = "commons-codec" % "commons-codec" % "1.10"
  val postgresql = "org.postgresql" % "postgresql" % "9.4.1208"
  val asynchttpclient = "org.asynchttpclient" % "async-http-client" % "2.0.32"
  val ehcache = "net.sf.ehcache" % "ehcache" % "2.10.4"
  val essf = "org.seekloud" %% "essf" % "0.0.1-beta1"



  val backendDependencies =
    Dependencies.akkaSeq ++
    Dependencies.akkaHttpSeq ++
    Dependencies.circeSeq ++
    Dependencies.slickLess ++
    Dependencies.slickPg ++
    Seq(
      Dependencies.scalaXml,
      Dependencies.slick,
      Dependencies.slickCodeGen,
      Dependencies.scalikejdbc,
      Dependencies.scalikejdbcConfig,
      Dependencies.scalatags,
      Dependencies.nscalaTime,
      Dependencies.hikariCP,
      Dependencies.logback,
      Dependencies.codec,
      Dependencies.postgresql,
      Dependencies.asynchttpclient,
      Dependencies.ehcache,
      Dependencies.essf
    )



}
