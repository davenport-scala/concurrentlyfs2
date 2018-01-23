package com.example.concurrentlyfs2

import cats.effect.IO
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

import scala.concurrent.ExecutionContext.Implicits.global


// Seriously Broken Leaks Socket AND Never Exits JVM
object HelloWorldServerBroken extends StreamApp[IO] with Http4sDsl[IO] {
  val service = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
  }

  def stream(args: List[String], requestShutdown: IO[Unit]) =
      BlazeBuilder[IO]
        .bindHttp(8080, "0.0.0.0")
        .mountService(service, "/")
        .serve
      .concurrently(
        BlazeBuilder[IO]
          .bindHttp(8081, "0.0.0.0")
          .mountService(service, "/")
          .serve
      )

}
