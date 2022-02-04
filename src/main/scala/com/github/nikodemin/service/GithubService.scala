package com.github.nikodemin.service

import com.github.nikodemin.exception.BusinessError
import com.github.nikodemin.model.ContributorInfo

trait GithubService[F[_]] {
  def getContributorsByCompany(companyName: String): F[Either[BusinessError, List[ContributorInfo]]]
}

object GithubService {

  private class GithubServiceLive[F[_]]() extends GithubService[F] {
    override def getContributorsByCompany(companyName: String): F[Either[BusinessError, List[ContributorInfo]]] = ???
  }

  def make[F[_]]: GithubService[F] = new GithubServiceLive[F]
}
