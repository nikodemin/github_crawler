package com.github.nikodemin.service

import cats.Applicative
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import com.github.nikodemin.client.Pagination
import com.github.nikodemin.model.dto.github.resp.RepositoryContributor
import fs2.{Pure, Stream}

import java.time.Instant
import scala.collection.mutable
import scala.concurrent.duration.Duration

trait ContributorsCache[F[_]] {

  def getRepoContributors(
    orgName: String,
    repoName: String,
    pagination: Pagination
  ): F[Option[List[RepositoryContributor]]]

  def saveRepoContributors(
    orgName: String,
    repoName: String,
    pagination: Pagination
  )(contributors: List[RepositoryContributor], expiration: Duration): F[Unit]
}

object ContributorsCache {
  type Key = (String, String, Pagination)

  /**
   * This could be redis for example
   */
  private class InMemoryContributorsCache[F[_]: Applicative] extends ContributorsCache[F] {
    val cache = mutable.Map.empty[Key, (Instant, List[RepositoryContributor])]

    override def getRepoContributors(
      orgName: String,
      repoName: String,
      pagination: Pagination
    ): F[Option[List[RepositoryContributor]]] = cache.filterInPlace { case _ -> (expires -> _) =>
      expires.isAfter(Instant.now)
    }.get((orgName, repoName, pagination)).map(_._2).pure[F]

    override def saveRepoContributors(
      orgName: String,
      repoName: String,
      pagination: Pagination
    )(contributors: List[RepositoryContributor], expiration: Duration): F[Unit] =
      cache.addOne((orgName, repoName, pagination), Instant.now.plusMillis(expiration.toMillis) -> contributors)
        .pure[F].as(())
  }

  def live[F[_]: Applicative]: ContributorsCache[F]                 = new InMemoryContributorsCache[F]()
  def stream[F[_]: Applicative]: Stream[Pure, ContributorsCache[F]] = Stream.emit(live)
}
