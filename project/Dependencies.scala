import sbt._

object Dependencies {

  trait Module {
    val group: String
    val artifacts: List[ModuleID]

    def withVersion(version: String)(artifact: String): ModuleID = group %% artifact % version

    def withJavaVersion(version: String)(artifact: String): ModuleID = group % artifact % version
  }

  object Versions {
    val catsEffect = "3.3.5"

    val tapir = "0.20.0-M8"
    val http4s = "0.23.9"

    val circe = "0.14.1"
    val enumeratum = "1.7.0"
    val chimney = "0.6.1"
    val newtype = "0.4.4"
    val pureconfig = "0.17.1"

    val scalatest = "3.2.11"
    val scalamock = "5.2.0"
    val testcontainersScala = "0.40.0"
    val `cats-effect-testing-scalatest` = "1.4.0"

    val slf4j = "1.7.35"
    val logback = "1.2.10"
    val log4cats = "2.2.0"
  }

  val circe: Module = new Module {
    override val group: String = "io.circe"

    override val artifacts: List[sbt.ModuleID] =
      List("circe-core", "circe-generic", "circe-generic-extras").map(withVersion(Versions.circe))
  }

  val tapir: Module = new Module {
    override val group: String = "com.softwaremill.sttp.tapir"

    override val artifacts: List[sbt.ModuleID] =
      List("tapir-core", "tapir-json-circe", "tapir-http4s-server").map(withVersion(Versions.tapir))
  }

  val typelevel: Module = new Module {
    override val group: String = "org.typelevel"

    override val artifacts: List[sbt.ModuleID] = List("cats-effect").map(
      withVersion(Versions.catsEffect)
    ) ++ List("log4cats-slf4j", "log4cats-core").map(withVersion(Versions.log4cats)) :+
      withVersion(Versions.`cats-effect-testing-scalatest`)("cats-effect-testing-scalatest") % Test
  }

  val scalatest: Module = new Module {
    override val group: String = "org.scalatest"
    override val artifacts: List[sbt.ModuleID] = List("scalatest").map(withVersion(Versions.scalatest))
  }

  val scalamock: Module = new Module {
    override val group: String = "org.scalamock"
    override val artifacts: List[sbt.ModuleID] = List("scalamock").map(withVersion(Versions.scalamock))
  }

  val dimafeng: Module = new Module {
    override val group: String = "com.dimafeng"

    override val artifacts: List[sbt.ModuleID] =
      List("testcontainers-scala-scalatest").map(withVersion(Versions.testcontainersScala))
  }

  val logback: Module = new Module {
    override val group: String = "ch.qos.logback"
    override val artifacts: List[sbt.ModuleID] = List("logback-classic").map(withJavaVersion(Versions.logback))
  }

  val slf4: Module = new Module {
    override val group: String = "org.slf4j"
    override val artifacts: List[sbt.ModuleID] = List("slf4j-api").map(withJavaVersion(Versions.slf4j))
  }

  val scalaland: Module = new Module {
    override val group: String = "io.scalaland"
    override val artifacts: List[sbt.ModuleID] = List("chimney").map(withVersion(Versions.chimney))
  }

  val beachape: Module = new Module {
    override val group: String = "com.beachape"
    override val artifacts: List[sbt.ModuleID] = List("enumeratum").map(withVersion(Versions.enumeratum))
  }

  val http4s: Module = new Module {
    override val group: String = "org.http4s"

    override val artifacts: List[sbt.ModuleID] =
      List("http4s-dsl", "http4s-circe", "http4s-blaze-server", "http4s-blaze-client").map(withVersion(Versions.http4s))
  }

  val estatico: Module = new Module {
    override val group: String = "io.estatico"
    override val artifacts: List[sbt.ModuleID] = List("newtype").map(withVersion(Versions.newtype))
  }

  val pureconfig: Module = new Module {
    override val group: String = "com.github.pureconfig"
    override val artifacts: List[sbt.ModuleID] = List("pureconfig").map(withVersion(Versions.pureconfig))
  }

  val live: List[sbt.ModuleID] =
    List(
      circe,
      tapir,
      typelevel,
      scalatest,
      scalamock,
      logback,
      slf4,
      scalaland,
      beachape,
      http4s,
      estatico,
      pureconfig
    ).flatMap(
      _.artifacts
    )

}
