package com.github.nikodemin.config

import cats.implicits.toBifunctorOps
import com.github.nikodemin.config.ConfigReaders.parseFailureToFailureReason
import org.http4s.{ParseFailure, Uri}
import pureconfig.ConfigReader
import pureconfig.error.{CannotConvert, ConfigReaderFailures, FailureReason}

trait ConfigReaders {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader.stringConfigReader.emap(value =>
      Uri.fromString(value).leftMap(parseFailureToFailureReason(value, Uri.getClass.getName))
    )
}

object ConfigReaders extends ConfigReaders {

  def parseFailureToFailureReason(value: String, toType: String)(parseResult: ParseFailure): FailureReason =
    CannotConvert(value, toType, parseResult.message)

  def configReaderFailuresToRuntimeException(errors: ConfigReaderFailures) =
    new RuntimeException(errors.prettyPrint(1))
}
