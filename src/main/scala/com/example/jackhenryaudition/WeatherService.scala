package com.example.jackhenryaudition

import cats.Applicative
import cats.data.EitherT
import cats.effect.Async
import com.example.jackhenryaudition.model.Weather._
import com.example.jackhenryaudition.model.{FailedToParseJson, ForecastError, InvalidUri}
import io.circe.Json
import org.http4s.Uri
import org.http4s.circe.jsonDecoder
import org.http4s.client.Client


trait WeatherService[F[_]]{
  def getTemp(n: Location): EitherT[F, ForecastError, Forecast]
}

object WeatherService {

  def impl[F[_]: Async](client: Client[F]): WeatherService[F] = new WeatherService[F]{
    def getTemp(location: Location): EitherT[F, ForecastError, Forecast] =
      for{
        gridPointsUri <- makeGridPointUri(location)
        pointsResponse <-EitherT.liftF(client.expect[Json](gridPointsUri))
        forecastUri <- getForecastUri(pointsResponse)
        forecastResponse <- EitherT.liftF(client.expect[Json](forecastUri))
        rawForecast <- getRawForecast(forecastResponse)
      } yield toForecast(rawForecast)
  }

  def makeGridPointUri[F[_]: Applicative](location: Location): EitherT[F, ForecastError, Uri] =
    EitherT.fromEither(
      Uri.fromString(s"https://api.weather.gov/points/${location.lattitude},${location.longitude}").toOption.toRight[ForecastError](InvalidUri)
    )

  def getForecastUri[F[_]: Applicative](j: Json): EitherT[F, ForecastError, Uri] = {
    EitherT.fromEither(
      j.hcursor
        .downField("properties")
        .downField("forecast")
        .as[String]
        .toOption.toRight[ForecastError](FailedToParseJson)
        .flatMap(s => Uri.fromString(s).toOption.toRight[ForecastError](InvalidUri))
    )
  }

  def getRawForecast[F[_]: Applicative](json: Json): EitherT[F, ForecastError, RawForecast] = {
    val period = json.hcursor
      .downField("properties")
      .downField("periods")
      .as[List[Json]]
      .toOption
      .getOrElse(Nil)
      .find(j =>
        j.hcursor
          .downField("number")
          .as[Int]
          .exists(_ == 1)
      )

    EitherT.fromEither((for {
      temp <- period.flatMap(p => p.hcursor.downField("temperature").as[Int].toOption)
      forecast <- period.flatMap(p => p.hcursor.downField("shortForecast").as[String].toOption)
    } yield RawForecast(temp, forecast))
      .toRight[ForecastError](FailedToParseJson))
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
