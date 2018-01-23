package com.example.concurrentlyfs2

import cats.effect.IO
import fs2.async.immutable.Signal
import fs2.{Stream, StreamApp}

import scala.concurrent.ExecutionContext.Implicits.global

object fs2NativeStreamBrokenInfinite extends StreamApp[IO] {

  def  serverBehaviorWeWant(string: String, terminated: Signal[IO, Boolean]): Stream[IO, StreamApp.ExitCode] = Stream.bracket(IO(println(s"Started $string")))(
    using => Stream.eval(terminated.discrete.takeWhile(_ == false).compile.drain) >>
      Stream.emit(StreamApp.ExitCode(0)).covary[IO] ,
    release => IO(println(s"Release $string"))
  )

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    for {
      exit <- Stream.eval(fs2.async.signalOf[IO, Boolean](false))
      out <- serverBehaviorWeWant("Stream1", exit)
        .concurrently(serverBehaviorWeWant("Stream2", exit))
    } yield out

}
