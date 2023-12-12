package com.example.jackhenryaudition

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = JackhenryauditionServer.run[IO]
}
