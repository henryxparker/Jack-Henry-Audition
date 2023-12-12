package com.example.jackhenryaudition

import cats.effect.{Async, MonadCancelThrow}
import cats.syntax.all._
import io.circe.generic.semiauto._
import io.circe.{Encoder, Json}
import org.http4s.circe.{jsonDecoder, jsonEncoderOf}
import org.http4s.client.Client
import org.http4s.headers.`User-Agent`
import org.http4s.{EntityEncoder, ProductId, Request, Uri}


trait WeatherService[F[_]]{
  def getTemp(n: WeatherService.Location): F[WeatherService.Forecast]
}

object WeatherService {
  case class Location(lattitude: Double, longitude: Double)
  case class RawForecast(temperature: Int, description: String)
  case class Forecast(forecast: String, temperature: String)
  object Forecast {
    implicit val forecastEncoder: Encoder[Forecast] = deriveEncoder
    implicit def forecastEntityEncoder[F[_]]: EntityEncoder[F, Forecast] = jsonEncoderOf
  }

  def addUserAgentHeader[F[_]: MonadCancelThrow](client: Client[F]): Client[F] = Client[F] {
    (req: Request[F]) =>
      client.run(
        req.withHeaders(`User-Agent`(ProductId("henrysjackhenryaudition")))
      )
  }

  def impl[F[_]: Async](client: Client[F]): WeatherService[F] = new WeatherService[F]{
    val fullclient = addUserAgentHeader(client)
    def getTemp(l: WeatherService.Location): F[Forecast] =
      for{
        pointsResponse <- fullclient.expect[Json](Uri.fromString(s"https://api.weather.gov/points/${l.lattitude},${l.longitude}").toOption.get)
        forecastResponse <- fullclient.expect[Json](getForecastUrl(pointsResponse))
      } yield toForecast(getRawForecast(forecastResponse))
  }

  def getForecastUrl(j: Json): Uri =
    j.hcursor
      .downField("properties")
      .downField("forecast")
      .as[String]
      .toOption
      .flatMap(s => Uri.fromString(s).toOption)
      .get

  def getRawForecast(json: Json): RawForecast = {
    val period = json.hcursor
      .downField("properties")
      .downField("periods")
      .downArray

    RawForecast(
      period.downField("temperature").as[Int].toOption.get,
      period.downField("shortForecast").as[String].toOption.get
    )
  }

  def toForecast(raw: RawForecast): Forecast =
    Forecast(
      raw.description,
      raw.temperature match {
      case x if x < -60 => "May god save us all"
      case x if x < 0 => "Face-bitingly cold"
      case x if x < 32 => "Very cold"
      case x if x < 50 => "Chilly"
      case x if x < 70 => "Moderate"
      case x if x < 85 => "Warm"
      case x if x < 110 => "Hot"
      case _ => "Don't go outside the air is angry"
      }
    )
}
