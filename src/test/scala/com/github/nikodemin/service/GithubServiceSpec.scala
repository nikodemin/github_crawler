package com.github.nikodemin.service

import cats.Id
import cats.implicits.catsSyntaxOptionId
import com.github.nikodemin.client.{GithubClient, Pagination}
import com.github.nikodemin.config.GithubConfig
import com.github.nikodemin.model.ContributorInfo
import com.github.nikodemin.model.dto.github.resp.{Repository, RepositoryContributor, RepositoryOwner}
import com.github.nikodemin.util.IdLoggerMock
import org.http4s.Uri
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.typelevel.log4cats.Logger

import scala.concurrent.duration.{Duration, DurationInt}
import scala.util.Random

class GithubServiceSpec extends AnyWordSpec with Matchers with MockFactory {
  val pageSize = 7
  val contributorNum = 30
  val contributors: List[String] = List.fill(contributorNum)(Random.nextString(7))
  val cacheExpiration = 30.seconds

  val githubClient: GithubClient[Id] = mock[GithubClient[Id]]
  val cache: ContributorsCache[Id] = mock[ContributorsCache[Id]]
  implicit val log: Logger[Id] = new IdLoggerMock()

  val githubConfig: GithubConfig =
    GithubConfig("", 3, Uri.unsafeFromString("http://localhost"), pageSize, cacheExpiration)
  val toPagination: Long => Pagination = num => Pagination(num, pageSize)
  val githubService: GithubService[Id] = GithubService.live[Id](githubClient, cache, githubConfig)

  "GithubService" should {
    "Paginate and return result" in {
      val companyName = "some company"

      val reposPage1 = createPaginatedRepos(1)
      val reposPage2 = createPaginatedRepos(1 + pageSize)
      val reposPage3 = createPaginatedRepos(1 + pageSize * 2, pageSize / 2)

      val contribPage1 = createPaginatedContributors(reposPage1)
      val contribPage2 = createPaginatedContributors(reposPage2)
      val contribPage3 = createPaginatedContributors(reposPage3)

      (githubClient.getOrganizationRepos _).expects(companyName, toPagination(1)).returns(reposPage1)
      (githubClient.getOrganizationRepos _).expects(companyName, toPagination(2)).returns(reposPage2)
      (githubClient.getOrganizationRepos _).expects(companyName, toPagination(3)).returns(reposPage3)

      (cache.getRepoContributors _).expects(*, *, *).returns(None).anyNumberOfTimes

      verifyContributorsCall(contribPage1 ++ contribPage2 ++ contribPage3)

      val expected: List[ContributorInfo] = calculateExpected(contribPage1 ++ contribPage2 ++ contribPage3)
      githubService.getContributorsByCompany(companyName).compile.toList shouldBe expected
    }

    "Process empty page" in {
      val companyName = "some company 3"

      (githubClient.getOrganizationRepos _).expects(companyName, toPagination(1)).returns(Nil)

      githubService.getContributorsByCompany(companyName).compile.toList shouldBe Nil
    }

    "Process empty page after non empty" in {
      val companyName = "some company 2"

      val reposPage1 = createPaginatedRepos(1)

      val contribPage1 = createPaginatedContributors(reposPage1)

      (githubClient.getOrganizationRepos _).expects(companyName, toPagination(1)).returns(reposPage1)
      (githubClient.getOrganizationRepos _).expects(companyName, toPagination(2)).returns(Nil)

      (cache.getRepoContributors _).expects(*, *, *).returns(None).anyNumberOfTimes

      verifyContributorsCall(contribPage1)

      val expected: List[ContributorInfo] = calculateExpected(contribPage1)
      githubService.getContributorsByCompany(companyName).compile.toList shouldBe expected

    }
  }

  def createPaginatedRepos(start: Int, num: Int = pageSize): List[Repository] =
    List.iterate(start, num)(_ + 1).map(i => Repository(s"repo$i", RepositoryOwner(s"owner$i")))

  def createPaginatedContributors(
    repos: List[Repository],
    pages: Int = 2
  ): Map[Repository, List[List[RepositoryContributor]]] = {
    def fillPage(pageSize: Int): List[RepositoryContributor] = List.fill(pageSize)(RepositoryContributor(
      contributors(Random.nextInt(contributors.size)),
      Random.nextInt(30)
    ))

    repos.map(repo => repo -> (List.fill(pages - 1)(fillPage(pageSize)) :+ fillPage(pageSize / 2))).toMap
  }

  def verifyContributorsCall(contribPage: Map[Repository, List[List[RepositoryContributor]]]): Unit =
    contribPage.foreach {
      case (repository, contribPages) => contribPages.zipWithIndex.foreach {
          case contributors -> pageIndex =>
            (githubClient.getRepoContributors _).expects(
              repository.owner.login,
              repository.name,
              toPagination(pageIndex + 1)
            ).returns(contributors)
            (cache.saveRepoContributors(_: String, _: String, _: Pagination)(
              _: List[RepositoryContributor],
              _: Duration
            )).expects(
              repository.owner.login,
              repository.name,
              toPagination(pageIndex + 1),
              contributors,
              cacheExpiration
            ).returns(())
        }
    }

  def calculateExpected(contribPage: Map[Repository, List[List[RepositoryContributor]]]): List[ContributorInfo] =
    contribPage.flatMap(_._2).flatten
      .foldLeft(Map.empty[String, Int])((map, c) =>
        map.updatedWith(c.login) {
          case Some(value) => (value + c.contributions).some
          case None        => c.contributions.some
        }
      )
      .toList
      .map((ContributorInfo.apply _).tupled)

}
