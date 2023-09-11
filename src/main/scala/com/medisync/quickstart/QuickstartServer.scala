package com.medisync.quickstart

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import pureconfig.ConfigSource
import cats.effect.kernel.ResourceAsync
import com.medisync.quickstart.Configuration.ServiceConf

import doobie._
import doobie.implicits._
import com.medisync.quickstart.outside.TestGateway

object QuickstartServer:

  def run[F[_]: Async]: F[Nothing] = {

    for {

      config <- Configuration.load[F]()
      client <- EmberClientBuilder.default[F].build
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)
      gw = TestGateway.impl[F](client)

      db <- Database.transactor(config.database) map { db =>
        Database.initialize(db); db
      }


      apService = AppointmentService.impl(db, gw)

      httpApp = (
        AppointmentController[F](apService)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      _ <-
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(config.server.port)
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
