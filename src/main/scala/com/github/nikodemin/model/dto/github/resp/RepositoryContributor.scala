package com.github.nikodemin.model.dto.github.resp

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class RepositoryContributor(login: String, contributions: Int)

object RepositoryContributor {
  implicit val codec: Codec.AsObject[RepositoryContributor] = deriveCodec[RepositoryContributor]
}
