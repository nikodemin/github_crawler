package com.github.nikodemin.model.dto.github.resp

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class Repository(name: String, owner: RepositoryOwner)

object Repository {
  implicit val codec: Codec.AsObject[Repository] = deriveCodec[Repository]
}
