package com.github.nikodemin.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class AppConfig(
  port: Int,
  github: GithubConfig
)

object AppConfig {
  implicit val reader: ConfigReader[AppConfig] = deriveReader[AppConfig]
}
