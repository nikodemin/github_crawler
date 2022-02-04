package com.github.nikodemin

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toBifunctorOps
import com.github.nikodemin.config.AppConfig
import com.github.nikodemin.endpoint.Endpoints.getContributorsByCompany
import com.github.nikodemin.service.GithubService
import org.http4s.HttpRoutes
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import pureconfig.ConfigSource
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = for {
    appConfig <- IO.fromEither(readConfig)
    client = BlazeClientBuilder[IO].withRetries(appConfig.github.maxRetryCount).resource
    _ <- BlazeServerBuilder[IO].bindHttp(appConfig.port)
      .withHttpApp(getRoutes(???).orNotFound)
      .serve
      .compile
      .drain
  } yield ExitCode.Success

  def getRoutes(githubService: GithubService[IO]): HttpRoutes[IO] =
    Http4sServerInterpreter[IO].toRoutes(getContributorsByCompany.serverLogic(githubService.getContributorsByCompany))

  lazy val readConfig: Either[RuntimeException, AppConfig] = ConfigSource.default.load[AppConfig]
    .leftMap(errors => new RuntimeException(errors.prettyPrint(1)))
}
