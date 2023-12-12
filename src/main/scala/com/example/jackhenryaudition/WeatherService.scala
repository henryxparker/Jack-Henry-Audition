package com.example.jackhenryaudition

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe._

trait WeatherService[F[_]]{
  def getTemp(n: WeatherService.Location): F[WeatherService.Temperature]
}

object WeatherService {
  case class Location(lattitude: Double, longitude: Double)

  final case class Temperature(Temperature: String) extends AnyVal
  object Temperature {
    implicit val TemperatureEncoder: Encoder[Temperature] = new Encoder[Temperature] {
      final def apply(a: Temperature): Json = Json.obj(
        ("message", Json.fromString(a.Temperature)),
      )
    }
    implicit def TemperatureEntityEncoder[F[_]]: EntityEncoder[F, Temperature] =
      jsonEncoderOf[F, Temperature]
  }

  def impl[F[_]: Applicative]: WeatherService[F] = new WeatherService[F]{
    def getTemp(l: WeatherService.Location): F[WeatherService.Temperature] =
        Temperature(s"${l.lattitude}, ${l.longitude}").pure[F]
  }
}
