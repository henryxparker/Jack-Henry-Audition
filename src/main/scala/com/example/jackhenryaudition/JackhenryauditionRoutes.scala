package com.example.jackhenryaudition

import cats.effect.Sync
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

  def weatherRoutes[F[_]: Sync](W: WeatherService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / DoubleVar(lattitude) / DoubleVar(longitude) =>
        for {
          temp <- W.getTemp(WeatherService.Location(lattitude, longitude))
          resp <- Ok(temp)
        } yield resp
    }
  }
}