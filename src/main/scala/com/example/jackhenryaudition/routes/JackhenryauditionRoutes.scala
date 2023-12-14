package com.example.jackhenryaudition.routes

import cats.effect.kernel.Concurrent
import cats.implicits._
import com.example.jackhenryaudition.WeatherService
import com.example.jackhenryaudition.model.Weather.Location
import com.example.jackhenryaudition.model.{FailedToParseJson, InvalidUri}
import com.example.jackhenryaudition.routes.RouteMatchers.DoubleVar
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object JackhenryauditionRoutes {

  def weatherRoutes[F[_]: Concurrent](W: WeatherService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / DoubleVar(lattitude) / DoubleVar(longitude) =>
        for {
          forecastResult <- W.getTemp(Location(lattitude, longitude)).value
          resp <- forecastResult.fold({
              case InvalidUri => InternalServerError("An error occurred, please contact support at yadda yadda: 123errorcode")
              case FailedToParseJson => InternalServerError("An error occurred please contact support yadda: errorcode2")
            },
            result => Ok(result)
          )
        } yield resp
    }
  }
}