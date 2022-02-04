package com.github.nikodemin.exception

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import io.circe.{Decoder, Encoder}

sealed trait BusinessError

object BusinessError {

  implicit val genDevConfig: Configuration = Configuration.default.withDiscriminator("type")

  case class GetOrganizationReposError(organization: String, httpStatus: Int) extends BusinessError

  case class GetRepoContributorsError(ownerLogin: String, repoName: String, httpStatus: Int) extends BusinessError

  implicit val decoder: Decoder[BusinessError] = implicitly[Decoder[BusinessError]]
  implicit val encoder: Encoder[BusinessError] = implicitly[Encoder[BusinessError]]
}
