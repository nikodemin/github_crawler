package com.github.nikodemin.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class ContributorInfo(name: String, contributions: Int)

object ContributorInfo {
  implicit val codec: Codec.AsObject[ContributorInfo] = deriveCodec[ContributorInfo]
}
