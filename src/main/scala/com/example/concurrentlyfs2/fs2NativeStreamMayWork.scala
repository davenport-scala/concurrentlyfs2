package com.example.concurrentlyfs2

import cats.effect.IO
import fs2.{StreamApp, Stream}

import scala.concurrent.ExecutionContext.Implicits.global

object fs2NativeStreamMayWork extends StreamApp[IO] {

  val bracketed : String => Stream[IO, StreamApp.ExitCode] = string =>  Stream.bracket(IO(println(s"Started $string")))(
    using => Stream.eval_(IO(())),
    release => IO(println(s"Release $string"))
  )

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    bracketed("Stream1")
    .concurrently(bracketed("Stream2"))


}
