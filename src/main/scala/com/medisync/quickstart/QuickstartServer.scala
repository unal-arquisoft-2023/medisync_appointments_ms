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

      db <- Database.transactor(config.database)
      _ <- Resource.eval(Database.initialize(db))

      client <- EmberClientBuilder.default[F].build
      gw = TestGateway.impl[F](client)

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
