package com.github.nikodemin

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toBifunctorOps
import com.github.nikodemin.client.{GithubClient, Pagination}
import com.github.nikodemin.config.AppConfig
import com.github.nikodemin.config.ConfigReaders.configReaderFailuresToRuntimeException
import com.github.nikodemin.endpoint.Endpoints.getContributorsByCompany
import com.github.nikodemin.service.GithubService
import org.http4s.HttpRoutes
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import pureconfig.ConfigSource
import sttp.tapir.server.http4s.Http4sServerInterpreter
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = stream.compile.drain.as(ExitCode.Success)

  def stream: Stream[IO, Unit] = for {
    appConfig                  <- Stream.eval(IO.fromEither(readConfig))
    implicit0(log: Logger[IO]) <- Stream.eval(Slf4jLogger.create[IO])
    client                     <- BlazeClientBuilder[IO].withRetries(appConfig.github.maxRetryCount).stream
      githubClient               <- GithubClient.stream(client, appConfig.github)
      githubService              <- GithubService.stream(githubClient, page => Pagination(page, appConfig.github.pageSize))
      _                          <-
        BlazeServerBuilder[IO].bindHttp(appConfig.port)
          .withHttpApp(getRoutes(githubService).orNotFound)
          .serve
  } yield ()

  def getRoutes(githubService: GithubService[IO]): HttpRoutes[IO] =
    Http4sServerInterpreter[IO].toRoutes(getContributorsByCompany.serverLogic(githubService.getContributorsByCompany))

  lazy val readConfig: Either[RuntimeException, AppConfig] = ConfigSource.default.load[AppConfig]
    .leftMap(configReaderFailuresToRuntimeException)
}
