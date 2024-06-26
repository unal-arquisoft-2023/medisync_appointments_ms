package com.medisync.quickstart

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

import com.medisync.quickstart.outside.TestGateway
import com.medisync.quickstart.availability.{AvailabilityRepository, AvailabilityController, AvailabilityService}

import cats.data.OptionT
import com.medisync.quickstart.appointment.{AppointmentRepository, AppointmentController, AppointmentService}

object QuickstartServer:

  def run[F[_]: Async]: F[Nothing] = {

    for {

      config <- Configuration.load[F]()

      db <- Database.transactor(config.database)
      _ <- Resource.eval(Database.initialize(db))

      avRep = AvailabilityRepository.impl[F](db)
      apRep = AppointmentRepository.impl[F](db)
      // _ <- Resource.eval(
      //   // apRep.bookAppointment(
      //   //   PatientId(1111),
      //   //   DoctorId(10),
      //   //   Specialty.Cardiology,
      //   //   LocalDate.now(),
      //   //   BlockId(9),
      //   //   MedicalRecordId(2323)
      //   // )
      //   // apRep.cancelAppointment(AppointmentId(7))
      //   apRep.getManyFromPatient(PatientId(222))
      //   map { println(_) }
      // )

      client <- EmberClientBuilder.default[F].build
      gw = TestGateway.impl[F](client)

      avService = AvailabilityService.impl(avRep)
      apService = AppointmentService.impl(apRep,avRep,gw)

      httpApp = (
        (AppointmentController[F](apService)) <+>
          (AvailabilityController[F](avService))
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
