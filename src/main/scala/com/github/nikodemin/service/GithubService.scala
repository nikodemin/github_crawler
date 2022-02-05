package com.github.nikodemin.service

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxSemigroup, toFlatMapOps, toFunctorOps}
import cats.{Applicative, Functor, Monad, Traverse}
import com.github.nikodemin.client.{GithubClient, Pagination}
import com.github.nikodemin.config.GithubConfig
import com.github.nikodemin.model.ContributorInfo
import com.github.nikodemin.model.dto.github.resp.{Repository, RepositoryContributor}
import fs2.Stream
import org.typelevel.log4cats.Logger

trait GithubService[F[_]] {
  def getContributorsByCompany(companyName: String): Stream[F, ContributorInfo]
}

object GithubService {

  private class GithubServiceLive[F[_]: Monad](
    githubClient: GithubClient[F],
    contributorsCache: ContributorsCache[F],
    githubConfig: GithubConfig
  )(implicit
    log: Logger[F]
  ) extends GithubService[F] {
    val toPagination: Long => Pagination = page => Pagination(page, githubConfig.pageSize)

    override def getContributorsByCompany(companyName: String): Stream[F, ContributorInfo] = {

      def repositoriesStream: Stream[F, Repository] =
        Stream.iterate(1)(_ + 1)
          .evalMap(page => githubClient.getOrganizationRepos(companyName, toPagination(page)))
          .takeThrough(_.size >= githubConfig.pageSize)
          .flatMap(Stream.emits(_))

      def contributorsStream(
        ownerLogin: String,
        repoName: String
      ): Stream[F, RepositoryContributor] =
        Stream.iterate(1)(_ + 1)
          .evalMap { page =>
            val key = (ownerLogin, repoName, toPagination(page))

            (contributorsCache.getRepoContributors _).tupled(key).flatMap {
              case Some(value) => value.pure[F]
              case None        => ((githubClient.getRepoContributors _).tupled(key)).map { contributors =>
                  (contributorsCache.saveRepoContributors _).tupled(key)(contributors)
                  contributors
                }
            }
          }
          .takeThrough(_.size >= githubConfig.pageSize)
          .flatMap(Stream.emits(_))

      Stream.eval(log.info(s"Getting contributors for company: $companyName")) >> repositoriesStream
        .flatMap(repo => contributorsStream(repo.owner.login, repo.name))
        .map(c => Map(c.login -> c.contributions))
        .reduce(_ combine _)
        .flatMap(map => Stream.emits(map.toSeq))
        .map((ContributorInfo.apply _).tupled)
    }
  }

  def live[F[_]: Monad](client: GithubClient[F], cache: ContributorsCache[F], config: GithubConfig)(
    implicit log: Logger[F]
  ): GithubService[F] = new GithubServiceLive[F](client, cache, config)

  def stream[F[_]: Monad](client: GithubClient[F], cache: ContributorsCache[F], config: GithubConfig)(
    implicit log: Logger[F]
  ): Stream[F, GithubService[F]] = Stream.emit(live(client, cache, config))
}
