package com.github.nikodemin.config

import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class GithubConfig(token: String, maxRetryCount: Int = 3, basePath: Uri)

object GithubConfig extends ConfigReaders {
  implicit val reader: ConfigReader[GithubConfig] = deriveReader[GithubConfig]
}
