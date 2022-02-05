package com.github.nikodemin.util

import cats.Id
import org.typelevel.log4cats.Logger

class IdLoggerMock extends Logger[Id] {
  override def error(t: Throwable)(message: => String): Id[Unit] = println(message)
  override def warn(t: Throwable)(message: => String): Id[Unit]  = println(message)
  override def info(t: Throwable)(message: => String): Id[Unit]  = println(message)
  override def debug(t: Throwable)(message: => String): Id[Unit] = println(message)
  override def trace(t: Throwable)(message: => String): Id[Unit] = println(message)
  override def error(message: => String): Id[Unit]               = println(message)
  override def warn(message: => String): Id[Unit]                = println(message)
  override def info(message: => String): Id[Unit]                = println(message)
  override def debug(message: => String): Id[Unit]               = println(message)
  override def trace(message: => String): Id[Unit]               = println(message)
}
