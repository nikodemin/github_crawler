package com.github.nikodemin.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

import scala.concurrent.duration.Duration

case class AppConfig(
  port: Int,
  timeout: Duration,
  github: GithubConfig
)

object AppConfig {
  implicit val reader: ConfigReader[AppConfig] = deriveReader[AppConfig]
}
