package com.github.nikodemin.client

import cats.effect.kernel.Concurrent
import com.github.nikodemin.config.GithubConfig
import com.github.nikodemin.http4s.Http4sCodecs
import com.github.nikodemin.model.dto.github.resp.{Repository, RepositoryContributor}
import fs2.Stream
import io.circe._
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, Header}
import org.typelevel.ci.CIStringSyntax

trait GithubClient[F[_]] {

  def getOrganizationRepos(
    orgName: String,
    pagination: Pagination
  ): F[List[Repository]]

  def getRepoContributors(
    ownerLogin: String,
    repoName: String,
    pagination: Pagination
  ): F[List[RepositoryContributor]]
}

object GithubClient {

  private class GithubClientLive[F[_]: Concurrent](client: Client[F], config: GithubConfig)(implicit
    repoDecoder: Decoder[Repository],
    repoContributorDecoder: Decoder[RepositoryContributor]
  ) extends GithubClient[F]
      with Http4sCodecs
      with Http4sClientDsl[F] {
    val bearerToken: Authorization = Authorization(Credentials.Token(AuthScheme.Bearer, config.token))
    val accept: Header.Raw = Header.Raw(ci"Accept", "application/vnd.github.v3+json")

    override def getOrganizationRepos(
      orgName: String,
      pagination: Pagination
    ): F[List[Repository]] = {
      val request = GET(
        config.basePath / "orgs" / orgName / "repos"
          withQueryParam ("page", pagination.pageNumber)
          withQueryParam ("per_page", pagination.pageSize),
        bearerToken,
        accept
      )
      client.expect[List[Repository]](request)
    }

    override def getRepoContributors(
      ownerLogin: String,
      repoName: String,
      pagination: Pagination
    ): F[List[RepositoryContributor]] = {
      val request = GET(
        config.basePath / "repos" / ownerLogin / repoName / "contributors"
          withQueryParam ("page", pagination.pageNumber)
          withQueryParam ("per_page", pagination.pageSize),
        bearerToken,
        accept
      )
      client.expect[List[RepositoryContributor]](request)
    }
  }

  def live[F[_]: Concurrent](client: Client[F], githubConfig: GithubConfig)(implicit
    repoDecoder: Decoder[Repository],
    repoContributorDecoder: Decoder[RepositoryContributor]
  ): GithubClient[F] = new GithubClientLive[F](client, githubConfig)

  def stream[F[_]: Concurrent](client: Client[F], githubConfig: GithubConfig)(implicit
    repoDecoder: Decoder[Repository],
    repoContributorDecoder: Decoder[RepositoryContributor]
  ): Stream[F, GithubClient[F]] = Stream.emit(live(client, githubConfig))
}
