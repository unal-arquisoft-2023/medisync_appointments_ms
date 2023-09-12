package com.medisync.quickstart

import java.time.Instant
import Appointments.Appointment
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
import outside.Gateway
import com.medisync.quickstart.Appointments._
import NewtypesDoobie._
import General._
import com.medisync.quickstart.Doctors._

trait AppointmentService[F[_]]:
  def create(patId: PatientId, docId: DoctorId, date: TimeRange, specialty: Specialty): F[AppointmentId]
  def cancel(appId: AppointmentId): F[Boolean]
  def attend(appId: AppointmentId): F[Boolean]
  def findOne(appId: AppointmentId): F[Option[Appointment]]
  def findAllByPatient(patId: PatientId): F[List[Appointment]]
  def findAllByDoctor(docId: DoctorId): F[List[Appointment]]


  def updateMissed: F[Int]

object AppointmentService:
  def apply[F[_]](implicit ev: AppointmentService[F]): AppointmentService[F] =
    ev

  def impl[F[_]: Async](T: Transactor[F], ds: DoctorService[F], gw: Gateway[F]) =
    new AppointmentService[F]:
      val dsl = new Http4sClientDsl[F] {}
      import dsl._
      def create(
          patId: PatientId,
          docId: DoctorId,
          date: TimeRange,
          specialty: Specialty
      ): F[AppointmentId] =
        for {

          // available <- ds.isAvailable(docId, specialty, DayOfWeek.Sunday, date)

          // _ <- Async[F].raiseWhen(!available)(new Error("Time and date not available"))

          medRecId <- gw.createMedicalRecord
          insert = sql"INSERT INTO appointment (doctor_id,patient_id,medical_record_id,start_time,end_time,status,notification_status,specialty) " ++
            sql"VALUES ($docId,$patId,$medRecId,${date.start},${date.end},${AppointmentStatus.Pending},${NotificationStatus.ToNotify},$specialty)"

          app <- insert.update
            .withUniqueGeneratedKeys[Appointment](
              "id",
              "start_time",
              "end_time",
              "doctor_id",
              "patient_id",
              "medical_record_id",
              "date_of_scheduling",
              "status",
              "notification_status",
              "specialty"
            )
            .transact(T)

          notified <- gw.createNotification(app)

          notifStatus =
            if notified then NotificationStatus.Notified
            else NotificationStatus.ToNotify

          update =
            sql"UPDATE appointment SET notification_status = ${notifStatus} WHERE id = ${app.id}"
          _ <- update.update.run.transact(T)
        } yield app.id

      def cancel(appId: AppointmentId): F[Boolean] =
        for {
          deleted <- gw.deleteNotification(appId)

          update =
            if deleted then
              sql"UPDATE appointment SET status = ${AppointmentStatus.Canceled} WHERE id = ${appId}"
            else
              sql"UPDATE appointment SET status = ${AppointmentStatus.Canceled}, notification_status = ${NotificationStatus.ToCancel} WHERE id = ${appId}"

          affectedRows <- update.update.run.transact(T)

        } yield affectedRows > 0

      def attend(appId: AppointmentId): F[Boolean] =
        for {
          affectedRows <-
            sql"UPDATE appointment SET status = ${AppointmentStatus.Attended} WHERE id = ${appId}".update.run
              .transact(T)
        } yield affectedRows > 0

      def findOne(appId: AppointmentId): F[Option[Appointment]] =
        for {
          app <-
            sql"select id, start_time, end_time, doctor_id, patient_id, medical_record_id, date_of_scheduling, status, notification_status, specialty from appointment where id = $appId"
              .query[Appointment]
              .option
              .transact(T)
        } yield app

      def findAllByPatient(patId: PatientId): F[List[Appointment]] =
        for {
          appL <-
            sql"select id, start_time, end_time, doctor_id, patient_id, medical_record_id, date_of_scheduling, status, notification_status, specialty from appointment where patient_id = $patId"
              .query[Appointment]
              .to[List]
              .transact(T)
        } yield appL

      def updateMissed: F[Int] = ???
        // update = sql"UPDATE appointments SET staus = ${AppointmentsStatus.Missed} WHERE "

      def findAllByDoctor(docId: DoctorId): F[List[Appointment]] =
        for {
          appL <-
            sql"select id, start_time, end_time, doctor_id, patient_id, medical_record_id, date_of_scheduling, status, notification_status, specialty from appointment where doctor_id = $docId"
              .query[Appointment]
              .to[List]
              .transact(T)
        } yield appL



