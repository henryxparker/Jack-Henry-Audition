package com.example.jackhenryaudition

import cats.effect.{Async, MonadCancelThrow}
import cats.syntax.all._
import io.circe.Json
import org.http4s.circe.{jsonDecoder, jsonEncoder}
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.{ProductId, Request, Response, Uri}
import org.http4s.headers.`User-Agent`


trait WeatherService[F[_]]{
  def getTemp(n: WeatherService.Location): F[Response[F]]
}

object WeatherService {
  case class Location(lattitude: Double, longitude: Double)
  case class Temperature(Temperature: String)

  def addUserAgentHeader[F[_]: MonadCancelThrow](client: Client[F]): Client[F] = Client[F] {
    (req: Request[F]) =>
      client.run(
        req.withHeaders(`User-Agent`(ProductId("henrysjackhenryaudition")))
      )
  }


  def impl[F[_]: Async](client: Client[F]): WeatherService[F] = new WeatherService[F]{
    val dsl = new Http4sDsl[F] {}
    import dsl._
    def getTemp(l: WeatherService.Location): F[Response[F]] =
      Uri.fromString(s"https://api.weather.gov/points/${l.lattitude},${l.longitude}")
        .toOption.toRight(BadRequest("unexpected input"))
        .map(uri => addUserAgentHeader(client).expect[Json](uri))
        .fold(r => r, r => r.flatMap(json => Ok(json)))
        
  }
}
