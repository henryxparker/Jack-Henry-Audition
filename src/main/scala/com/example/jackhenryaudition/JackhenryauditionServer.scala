package com.example.jackhenryaudition

import cats.effect.{Async, MonadCancelThrow}
import com.comcast.ip4s._
import com.example.jackhenryaudition.routes.JackhenryauditionRoutes
import fs2.io.net.Network
import org.http4s.{ProductId, Request}
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.headers.`User-Agent`
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object JackhenryauditionServer {

  def run[F[_]: Async: Network]: F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build
      weatherServiceAlg = WeatherService.impl[F](addUserAgentHeader(client))

      httpApp = JackhenryauditionRoutes.weatherRoutes[F](weatherServiceAlg).orNotFound

      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever

  def addUserAgentHeader[F[_] : MonadCancelThrow](client: Client[F]): Client[F] = Client[F] {
    (req: Request[F]) =>
      client.run(
        req.withHeaders(`User-Agent`(ProductId("henrysjackhenryaudition")))
      )
  }
}
