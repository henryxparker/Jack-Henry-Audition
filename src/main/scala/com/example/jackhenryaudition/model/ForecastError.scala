package com.example.jackhenryaudition.model

sealed trait ForecastError
case object InvalidUri extends ForecastError //could change to include specific failure info
case object FailedToParseJson extends ForecastError // ^^