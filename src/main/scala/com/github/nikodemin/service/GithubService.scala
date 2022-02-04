package com.github.nikodemin.service

import cats.implicits._
import cats.{ApplicativeError, Monad}
import com.github.nikodemin.client.GithubClient
import com.github.nikodemin.exception.BusinessError
import com.github.nikodemin.model.ContributorInfo
import fs2.Stream
import org.typelevel.log4cats.Logger

trait GithubService[F[_]] {
  def getContributorsByCompany(companyName: String): F[Either[BusinessError, List[ContributorInfo]]]
}

object GithubService {

  private class GithubServiceLive[F[_]: Monad](githubClient: GithubClient[F])(implicit log: Logger[F])
      extends GithubService[F] {

    override def getContributorsByCompany(companyName: String): F[Either[BusinessError, List[ContributorInfo]]] =
      for {
        _            <- log.info(s"Getting contributors for company: $companyName")
        reposOrError <- githubClient.getOrganizationRepos(companyName)
        _            <- log.info(s"REPOS: $reposOrError")
      } yield BusinessError.GetOrganizationReposError(companyName, 400).asLeft

  }

  def live[F[_]: Monad](client: GithubClient[F])(implicit log: Logger[F]): GithubService[F] =
    new GithubServiceLive[F](client)

  def stream[F[_]: Monad](client: GithubClient[F])(implicit log: Logger[F]): Stream[F, GithubService[F]] =
    Stream.emit(live(client))
}
