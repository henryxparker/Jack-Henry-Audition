package com.example.jackhenryaudition

import cats.effect.kernel.Concurrent
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import scala.util.Try

object JackhenryauditionRoutes {

  object DoubleVar {
    def unapply(str: String): Option[Double] =
      if (!str.isEmpty)
        Try(str.toDouble).toOption
      else
        None
  }

  def weatherRoutes[F[_]: Concurrent](W: WeatherService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / DoubleVar(lattitude) / DoubleVar(longitude) =>
        for {
          forecast <- W.getTemp(WeatherService.Location(lattitude, longitude))
          resp <- Ok(forecast)
        } yield resp
    }
  }
}