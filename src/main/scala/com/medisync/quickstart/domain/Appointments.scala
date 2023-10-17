package com.medisync.quickstart.domain
import java.time.Instant
import monix.newtypes._

import monix.newtypes.integrations.DerivedCirceCodec
import doobie.util.meta.Meta
import com.medisync.quickstart.utilities.NewtypesDoobie._
import com.medisync.quickstart.utilities.NewtypesHttp4s._
import Doctors._

import cats.Show

import scala.util.Try

import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres._
import doobie.postgres.implicits._
import org.http4s.CacheDirective.public
import io.circe._
import io.circe.syntax._
import java.time.LocalDate
import java.time.LocalTime
import com.medisync.quickstart.utilities.{NewtypesDoobie, NewtypesHttp4s}

object Appointments:

  type TestId = TestId.Type
  object TestId
      extends NewtypeWrapped[Int]
      with DerivedCirceCodec
      with DerivedDoobieCodec
      with DerivedHttp4sParamCodec

  type AppointmentId = AppointmentId.Type
  object AppointmentId
      extends NewtypeWrapped[Int]
      with DerivedCirceCodec
      with DerivedDoobieCodec
      with DerivedHttp4sParamCodec

  type BlockId = BlockId.Type
  object BlockId
      extends NewtypeWrapped[Int]
      with DerivedCirceCodec
      with DerivedDoobieCodec
      with DerivedHttp4sParamCodec


  type MedicalRecordId = MedicalRecordId.Type
  object MedicalRecordId
      extends NewtypeWrapped[Int]
      with DerivedCirceCodec
      with DerivedDoobieCodec
      with DerivedHttp4sParamCodec

  type PatientId = PatientId.Type
  object PatientId
      extends NewtypeWrapped[String]
      with DerivedCirceCodec
      with DerivedDoobieCodec
      with DerivedHttp4sParamCodec

  enum AppointmentStatus extends Enum[AppointmentStatus]:
    case Pending, Canceled, Attended, Missed

  object AppointmentStatus:
    given Meta[AppointmentStatus] =
      pgJavaEnum[AppointmentStatus]("appointment_status_enum")
    
    given Encoder[AppointmentStatus] =
      Encoder.encodeString.contramap[AppointmentStatus](_.toString())

  enum NotificationStatus extends Enum[NotificationStatus]:
    case ToNotify, Notified, ToCancel, Canceled

  object NotificationStatus:
    given Meta[NotificationStatus] =
      pgJavaEnum[NotificationStatus]("notification_status_enum")
    given Encoder[NotificationStatus] =
      Encoder.encodeString.contramap[NotificationStatus](_.toString())

  case class AppointmentRecord(
      id: AppointmentId,
      date: LocalDate,
      blockId: BlockId,
      doctorId: DoctorId,
      patientId: PatientId,
      medicalRecordId: MedicalRecordId,
      scheduledTimestamp: Instant,
      status: AppointmentStatus,
      notificationStatus: NotificationStatus,
      specialty: Specialty
  )

  object AppointmentRecord:
    given Encoder[AppointmentRecord] = new Encoder[AppointmentRecord]:
      final def apply(app: AppointmentRecord): Json = Json.obj(
        ("id", app.id.asJson),
        ("date", app.date.asJson),
        ("block_id", app.blockId.asJson),
        ("doctor_id", app.doctorId.asJson),
        ("patiend_id", app.patientId.asJson),
        ("medical_record_id", app.medicalRecordId.asJson),
        ("date_of_scheduling", app.scheduledTimestamp.asJson),
        ("status", app.status.asJson),
        ("notification_status", app.notificationStatus.asJson),
        ("specialty", app.specialty.asJson)
      )
    given Read[AppointmentRecord] =
      Read[
        (
            AppointmentId,
            LocalDate,
            BlockId,
            DoctorId,
            PatientId,
            MedicalRecordId,
            Instant,
            AppointmentStatus,
            NotificationStatus,
            Specialty
        )
      ].map {
        case (
              id,
              date,
              blockId,
              doctorId,
              patientId,
              medicalRecordId,
              scheduledTimestamp,
              status,
              notificationStatus,
              specialty
            ) =>
          AppointmentRecord(
            id,
            date,
            blockId,
            doctorId,
            patientId,
            medicalRecordId,
            scheduledTimestamp,
            status,
            notificationStatus,
            specialty
          )
      }

  given Write[AppointmentRecord] =
    Write[
      (
          AppointmentId,
          LocalDate,
          DoctorId,
          PatientId,
          MedicalRecordId,
          Instant,
          AppointmentStatus,
          NotificationStatus,
          Specialty
      )
    ]
      .contramap(app =>
        (
          app.id,
          app.date,
          app.doctorId,
          app.patientId,
          app.medicalRecordId,
          app.scheduledTimestamp,
          app.status,
          app.notificationStatus,
          app.specialty
        )
      )

  case class AvailableAppointment(
    date: LocalDate,
    block: TimeBlock,
    doctorId: DoctorId,
  )

  object AvailableAppointment:
    given Encoder[AvailableAppointment] = new Encoder[AvailableAppointment]:
      final def apply(app: AvailableAppointment): Json = Json.obj(
        ("date", app.date.asJson),
        ("block", app.block.asJson),
        ("doctor_id", app.doctorId.asJson),
      )
    
    given Read[AvailableAppointment] =
      Read[
        (
            LocalDate,
            BlockId,
            DoctorId,
            LocalTime,
            LocalTime
        )
      ].map {
        case (
              date,
              blockId,
              doctorId,
              startTime,
              endTime
            ) =>
          AvailableAppointment(
            date,
            TimeBlock(blockId, startTime, endTime),
            doctorId
          )
      }

  case class Appointment(
      id: AppointmentId,
      date: LocalDate,
      startTime: LocalTime,
      endTime: LocalTime,
      blockId: BlockId,
      doctorId: DoctorId,
      patientId: PatientId,
      medicalRecordId: MedicalRecordId,
      scheduledTimestamp: Instant,
      status: AppointmentStatus,
      notificationStatus: NotificationStatus,
      specialty: Specialty
  )


  object Appointment:

    given Encoder[Appointment] = new Encoder[Appointment]:
      final def apply(app: Appointment): Json = Json.obj(
        ("id", app.id.asJson),
        ("date", app.date.asJson),
        (
          "time",
          Json.obj(("start", app.startTime.asJson), ("end", app.endTime.asJson))
        ),
        ("doctor_id", app.doctorId.asJson),
        ("patiend_id", app.patientId.asJson),
        ("medical_record_id", app.medicalRecordId.asJson),
        ("date_of_scheduling", app.scheduledTimestamp.asJson),
        ("status", app.status.asJson),
        ("notification_status", app.notificationStatus.asJson),
        ("specialty", app.specialty.asJson)
      )
