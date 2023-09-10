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

object QuickstartServer:

  def run[F[_]: Async]: F[Nothing] = {

    for {


      client <- EmberClientBuilder.default[F].build
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)

      config <- Configuration.load[F]() map {
        case c: ServiceConf => c
      }

      _ <- Resource.eval(Async[F].blocking(println(config)))
      db <- Database.transactor(config.database)
      // _ <- Resource.eval(sql"select name from actors".query[String].to[List].transact[F](db)).map(println(_))
      _ <- Resource.eval(Database.initialize(db))
      
      apService = AppointmentService.impl(db,client)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
        // QuickstartRoutes.jokeRoutes[F](jokeAlg)
        QuickstartRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
        AppointmentController[F](apService)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(config.server.port)
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
