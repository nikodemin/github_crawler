package com.github.nikodemin.http4s

import cats.effect.kernel.Concurrent
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}

trait Http4sCodecs {
  implicit def encoder[F[_], T: Encoder]: EntityEncoder[F, T] = jsonEncoderOf

  implicit def decoder[F[_] : Concurrent, T: Decoder]: EntityDecoder[F, T] = jsonOf
}

object Http4sCodecs extends Http4sCodecs
