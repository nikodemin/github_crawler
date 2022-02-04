package com.github.nikodemin.exception

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

sealed trait BusinessError

object BusinessError {
  case class GetOrganizationReposError(organization: String, httpStatus: Int) extends BusinessError

  case class GetRepoContributorsError(ownerLogin: String, repoName: String, httpStatus: Int) extends BusinessError

  implicit val genDevConfig: Configuration          = Configuration.default.withDiscriminator("type")
  implicit val codec: Codec.AsObject[BusinessError] = deriveConfiguredCodec[BusinessError]
}
