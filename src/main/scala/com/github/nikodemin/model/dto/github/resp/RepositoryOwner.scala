package com.github.nikodemin.model.dto.github.resp

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class RepositoryOwner(login: String)

object RepositoryOwner {
  implicit val codec: Codec.AsObject[RepositoryOwner] = deriveCodec[RepositoryOwner]
}
