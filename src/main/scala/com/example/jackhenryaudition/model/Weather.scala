package com.example.jackhenryaudition.model

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

object Weather {
  case class Location(lattitude: Double, longitude: Double)

  case class RawForecast(temperature: Int, description: String)

  case class Forecast(forecast: String, temperature: String)

  object Forecast {
    implicit val forecastEncoder: Encoder[Forecast] = deriveEncoder
    implicit def forecastEntityEncoder[F[_]]: EntityEncoder[F, Forecast] = jsonEncoderOf
  }

}
