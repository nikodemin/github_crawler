package com.github.nikodemin.config

import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

import scala.concurrent.duration.Duration

case class GithubConfig(
  token: String,
  maxRetryCount: Int = 3,
  basePath: Uri,
  pageSize: Int = 30,
  cacheExpiration: Duration
)

object GithubConfig extends ConfigReaders {
  implicit val reader: ConfigReader[GithubConfig] = deriveReader[GithubConfig]
}
