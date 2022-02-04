package com.github.nikodemin.client

import cats.effect.kernel.Concurrent
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId, toFunctorOps}
import com.github.nikodemin.config.GithubConfig
import com.github.nikodemin.exception.BusinessError
import com.github.nikodemin.exception.BusinessError.{GetOrganizationReposError, GetRepoContributorsError}
import com.github.nikodemin.http4s.Http4sCodecs
import com.github.nikodemin.model.dto.github.resp.{Repository, RepositoryContributor}
import fs2.Stream
import io.circe._
import org.http4s.Method.GET
import org.http4s.Status.Successful
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, Header, Request, Response}
import org.typelevel.ci.CIStringSyntax

trait GithubClient[F[_]] {

  def getOrganizationRepos(
    orgName: String,
    pagination: Pagination
  ): F[Either[GetOrganizationReposError, List[Repository]]]

  def getRepoContributors(
    ownerLogin: String,
    repoName: String,
    pagination: Pagination
  ): F[Either[GetRepoContributorsError, List[RepositoryContributor]]]
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
    ): F[Either[GetOrganizationReposError, List[Repository]]] = {
      val request = GET(
        config.basePath / "orgs" / orgName / "repos"
          withQueryParam ("page", pagination.pageNumber)
          withQueryParam ("per_page", pagination.pageSize),
        bearerToken,
        accept
      )
      runWithErrorHandling(request)(resp => GetOrganizationReposError(orgName, resp.status.code))
    }

    override def getRepoContributors(
      ownerLogin: String,
      repoName: String,
      pagination: Pagination
    ): F[Either[GetRepoContributorsError, List[RepositoryContributor]]] = {
      val request = GET(
        config.basePath / "repos" / ownerLogin / repoName / "contributors"
          withQueryParam ("page", pagination.pageNumber)
          withQueryParam ("per_page", pagination.pageSize),
        bearerToken,
        accept
      )
      runWithErrorHandling(request)(resp => GetRepoContributorsError(ownerLogin, repoName, resp.status.code))
    }

    private def runWithErrorHandling[T: Decoder, E <: BusinessError](request: Request[F])(onError: Response[F] => E)
      : F[Either[E, T]] =
      client.run(request).use {
        case Successful(resp) => resp.as[T].map(_.asRight)
        case resp             => onError(resp).asLeft[T].pure[F]
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
