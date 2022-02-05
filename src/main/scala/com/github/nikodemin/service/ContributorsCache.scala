package com.github.nikodemin.service

import cats.Applicative
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import com.github.nikodemin.client.Pagination
import com.github.nikodemin.model.dto.github.resp.RepositoryContributor
import fs2.{Pure, Stream}

import scala.collection.mutable

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
  )(contributors: List[RepositoryContributor]): F[Unit]
}

object ContributorsCache {
  type Key = (String, String, Pagination)

  private class InMemoryContributorsCache[F[_]: Applicative] extends ContributorsCache[F] {
    val cache = mutable.Map.empty[Key, List[RepositoryContributor]]

    override def getRepoContributors(
      orgName: String,
      repoName: String,
      pagination: Pagination
    ): F[Option[List[RepositoryContributor]]] = cache.get((orgName, repoName, pagination)).pure[F]

    override def saveRepoContributors(
      orgName: String,
      repoName: String,
      pagination: Pagination
    )(contributors: List[RepositoryContributor]): F[Unit] =
      cache.addOne((orgName, repoName, pagination), contributors).pure[F].as(())
  }

  def live[F[_]: Applicative]: ContributorsCache[F]                 = new InMemoryContributorsCache[F]()
  def stream[F[_]: Applicative]: Stream[Pure, ContributorsCache[F]] = Stream.emit(live)
}
