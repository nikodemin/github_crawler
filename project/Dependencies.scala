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
    val circe = "0.15.0-M1"
    val enumeratum = "1.7.0"
    val chimney = "0.6.1"

    val scalatest = "3.2.11"
    val scalamock = "5.2.0"
    val testcontainersScala = "0.40.0"
    val `cats-effect-testing-scalatest` = "1.4.0"

    val slf4j = "1.7.35"
    val logback = "1.2.10"
  }

  val circe: Module = new Module {
    override val group: String = "io.circe"
    override val artifacts: List[sbt.ModuleID] = List("circe-core", "circe-generic").map(withVersion(Versions.circe))
  }

  val sttp: Module = new Module {
    override val group: String = "com.softwaremill.sttp.tapir"
    override val artifacts: List[sbt.ModuleID] = List("tapir-core", "tapir-json-circe").map(withVersion(Versions.tapir))
  }

  val typelevel: Module = new Module {
    override val group: String = "org.typelevel"

    override val artifacts: List[sbt.ModuleID] = List("cats-effect").map(
      withVersion(Versions.catsEffect)
    ) :+ withVersion(Versions.`cats-effect-testing-scalatest`)("cats-effect-testing-scalatest") % Test
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

  val beachape = new Module {
    override val group: String = "com.beachape"
    override val artifacts: List[sbt.ModuleID] = List("enumeratum").map(withVersion(Versions.enumeratum))
  }

  val live: List[sbt.ModuleID] =
    List(circe, sttp, typelevel, scalatest, scalamock, logback, slf4, scalaland, beachape).flatMap(_.artifacts)

}
