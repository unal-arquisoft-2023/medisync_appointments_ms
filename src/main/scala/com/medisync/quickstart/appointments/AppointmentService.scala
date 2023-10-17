package com.medisync.quickstart.appointment

import java.time.Instant
import cats.effect.Async
import cats.implicits._
import io.circe.{Encoder, Decoder}
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._
import org.http4s.Method._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.postgres.pgisimplicits._
import java.time.{LocalDate, LocalTime}
import com.medisync.quickstart.domain.Appointments._
import com.medisync.quickstart.domain.Doctors._
import com.medisync.quickstart.outside.Gateway
import com.medisync.quickstart.availability.AvailabilityRepository

trait AppointmentService[F[_]]:
  def create(
      patId: PatientId,
      docId: DoctorId,
      date: LocalDate,
      specialty: Specialty,
      blockId: BlockId
  ): F[AppointmentId]
  def cancel(appId: AppointmentId): F[Boolean]
  def attend(appId: AppointmentId): F[Boolean]
  def findAllByPatient(patId: PatientId): F[List[AppointmentRecord]]
  def findOne(apId: AppointmentId): F[Option[AppointmentRecord]]
  def findAllByDoctor(docId: DoctorId): F[List[AppointmentRecord]]
  def findAllBySpecialty(spe: Specialty): F[List[AvailableAppointment]]

  def updateMissed: F[Int]

object AppointmentService:
  def apply[F[_]](implicit ev: AppointmentService[F]): AppointmentService[F] =
    ev

  def impl[F[_]: Async](apRep: AppointmentRepository[F], avRep: AvailabilityRepository[F], gw: Gateway[F]) =
    new AppointmentService[F]:
      val dsl = new Http4sClientDsl[F] {}
      import dsl._
      def create(
          patId: PatientId,
          docId: DoctorId,
          date: LocalDate,
          specialty: Specialty,
          blockId: BlockId
      ): F[AppointmentId] =
        for {
          hasAppointment <- avRep.hasAppointment(docId,specialty,date,blockId)
          _ = println("here")
          _ <- Async[F].raiseWhen(hasAppointment)(Exception("That appointment is already booked"))
          _ = println("here2")
          medRecId <- gw.createMedicalRecord
          _ = println("here2")
          ap <- apRep.bookAppointment(
            patId,
            docId,
            specialty,
            date,
            blockId,
            medRecId
          )
        } yield ap.id

      def cancel(appId: AppointmentId): F[Boolean] = 
        for {
          r <- apRep.cancelAppointment(appId)
        } yield r

      def attend(appId: AppointmentId): F[Boolean] = 
        for {
          r <- apRep.attendAppointment(appId)
        } yield r


      def findAllByPatient(patId: PatientId): F[List[AppointmentRecord]] = 
        for {
          r <- apRep.getManyFromPatient(patId)
        } yield r

      def findOne(apId: AppointmentId): F[Option[AppointmentRecord]] = 
        for {
          r <- apRep.findOne(apId)
        } yield r



      def updateMissed: F[Int] = ???
      // update = sql"UPDATE appointments SET staus = ${AppointmentsStatus.Missed} WHERE "

      def findAllByDoctor(docId: DoctorId): F[List[AppointmentRecord]] = 
        for {
          r <- apRep.getManyFromDoctor(docId)
        } yield r

      def findAllBySpecialty(spe: Specialty): F[List[AvailableAppointment]] = 
        for {
          r <- apRep.getManyAvailableFromSpecialty(spe)
        } yield r