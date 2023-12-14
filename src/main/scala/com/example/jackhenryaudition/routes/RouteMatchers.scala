package com.example.jackhenryaudition.routes

import scala.util.Try

object RouteMatchers {

  object DoubleVar {
    def unapply(str: String): Option[Double] =
      if (!str.isEmpty)
        Try(str.toDouble).toOption
      else
        None
  }

}
