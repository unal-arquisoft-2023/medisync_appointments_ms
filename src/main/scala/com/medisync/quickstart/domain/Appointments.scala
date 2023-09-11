package com.medisync.quickstart
import java.time.Instant
import monix.newtypes._

import monix.newtypes.integrations.DerivedCirceCodec
import doobie.util.meta.Meta
import NewtypesDoobie._

import cats.Show
import NewtypesHttp4s._

import scala.util.Try

import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres._
import doobie.postgres.implicits._
import org.http4s.CacheDirective.public
import com.medisync.quickstart.Doctors.DoctorId
import io.circe._
import io.circe.syntax._
import General._
import com.medisync.quickstart.Doctors.Specialty

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

  type MedicalRecordId = MedicalRecordId.Type
  object MedicalRecordId
      extends NewtypeWrapped[Int]
      with DerivedCirceCodec
      with DerivedDoobieCodec
      with DerivedHttp4sParamCodec

  type PatientId = PatientId.Type
  object PatientId
      extends NewtypeWrapped[Int]
      with DerivedCirceCodec
      with DerivedDoobieCodec
      with DerivedHttp4sParamCodec

  enum AppointmentStatus extends Enum[AppointmentStatus]:
    case Pending, Canceled, Attended, Missed

  given Meta[AppointmentStatus] =
    pgJavaEnum[AppointmentStatus]("appointment_status_enum")

  enum NotificationStatus extends Enum[NotificationStatus]:
    case ToNotify, Notified, ToCancel, Canceled

  given Meta[NotificationStatus] =
    pgJavaEnum[NotificationStatus]("notification_status_enum")


  case class Appointment(
      id: AppointmentId,
      timeRange: TimeRange,
      doctorId: DoctorId,
      patientId: PatientId,
      medicalRecordId: MedicalRecordId,
      dateOfScheduling: Instant,
      status: AppointmentStatus,
      notificationStaus: NotificationStatus,
      specialty: Specialty
  )

  object  Appointment:

    given Encoder[AppointmentStatus] = Encoder.encodeString.contramap[AppointmentStatus](_.toString())
    given Encoder[NotificationStatus] = Encoder.encodeString.contramap[NotificationStatus](_.toString())
    given Encoder[Appointment] = new Encoder[Appointment]:
      final def apply(app: Appointment): Json = Json.obj(
        ("id",app.id.asJson),
        ("date",app.timeRange.asJson),
        ("doctor_id",app.doctorId.asJson),
        ("patiend_id",app.patientId.asJson),
        ("medical_record_id",app.medicalRecordId.asJson),
        ("date_of_scheduling",app.dateOfScheduling.asJson),
        ("status", app.status.asJson),
        ("notification_status",app.notificationStaus.asJson),
        ("specialty",app.specialty.asJson)
      ) 


    given Read[Appointment] =
      Read[
        (
            AppointmentId,
            TimeRange,
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
              appId,
              time,
              docId,
              patId,
              medRecId,
              dateOfSch,
              status,
              notifStatus,
              spe
            ) =>
          Appointment(
            appId,
            time,
            docId,
            patId,
            medRecId,
            dateOfSch,
            status,
            notifStatus,
            spe
          )
    }
