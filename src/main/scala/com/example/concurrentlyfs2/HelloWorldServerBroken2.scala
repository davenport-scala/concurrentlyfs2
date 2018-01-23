package com.example.concurrentlyfs2

import cats.effect.IO
import io.circe._
import fs2.Stream
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

import scala.concurrent.ExecutionContext.Implicits.global


// Leaks Socket Connection on Ctrl-C
object HelloWorldServerBroken2 extends StreamApp[IO] with Http4sDsl[IO] {
  val service = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
  }

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    Stream(
      BlazeBuilder[IO]
        .bindHttp(8080, "0.0.0.0")
        .mountService(service, "/")
        .serve,
      BlazeBuilder[IO]
        .bindHttp(8081, "0.0.0.0")
        .mountService(service, "/")
        .serve
    ).join(2)

}
