package com.github.nikodemin.service

import cats.Monad
import cats.implicits._
import com.github.nikodemin.client.{GithubClient, Pagination}
import com.github.nikodemin.exception.BusinessError
import com.github.nikodemin.exception.BusinessError.{GetOrganizationReposError, GetRepoContributorsError}
import com.github.nikodemin.model.ContributorInfo
import com.github.nikodemin.model.dto.github.resp.{Repository, RepositoryContributor}
import fs2.Stream
import org.typelevel.log4cats.Logger

trait GithubService[F[_]] {
  def getContributorsByCompany(companyName: String): F[Either[BusinessError, List[ContributorInfo]]]
}

object GithubService {

  private class GithubServiceLive[F[_]: Monad](githubClient: GithubClient[F], toPagination: Long => Pagination)(implicit
    log: Logger[F]
  ) extends GithubService[F] {

    override def getContributorsByCompany(companyName: String): F[Either[BusinessError, List[ContributorInfo]]] = {
      def repositoriesStream: Stream[F, Either[GetOrganizationReposError, Repository]] =
        Stream.iterate(1)(_ + 1).evalMap(page => githubClient.getOrganizationRepos(companyName, toPagination(page)))
          .takeThrough(_.fold(_ => false, _.nonEmpty))
          .flatMap(e => Stream.emits(e.sequence))

      def contributorsStream(
        ownerLogin: String,
        repoName: String
      ): Stream[F, Either[GetRepoContributorsError, RepositoryContributor]] =
        Stream.iterate(1)(_ + 1).evalMap(page =>
          githubClient.getRepoContributors(ownerLogin, repoName, toPagination(page))
        ).takeThrough(_.fold(_ => false, _.nonEmpty))
          .flatMap(e => Stream.emits(e.sequence))

      def resultStream = repositoriesStream

      for {
        _            <- log.info(s"Getting contributors for company: $companyName")
        reposOrError <- githubClient.getOrganizationRepos(companyName, toPagination(10))
        _            <- log.info(s"REPOS: $reposOrError")
      } yield BusinessError.GetOrganizationReposError(companyName, 400).asLeft
    }

  }

  def live[F[_]: Monad](client: GithubClient[F], toPagination: Long => Pagination)(implicit
    log: Logger[F]
  ): GithubService[F] =
    new GithubServiceLive[F](client, toPagination)

  def stream[F[_]: Monad](client: GithubClient[F], toPagination: Long => Pagination)(implicit
    log: Logger[F]
  ): Stream[F, GithubService[F]] =
    Stream.emit(live(client, toPagination))
}
