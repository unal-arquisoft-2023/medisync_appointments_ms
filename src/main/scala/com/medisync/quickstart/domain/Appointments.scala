package com.medisync.quickstart
import java.time.Instant
import monix.newtypes._

import monix.newtypes.integrations.DerivedCirceCodec
import doobie.util.meta.Meta
import NewtypesDoobie._

import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import cats.Show
import NewtypesHttp4s._

object Appointments {
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

  case class Appointment(
      id: AppointmentId,
      date: Instant,
      doctorId: Int,
      patientId: PatientId,
      medicalRecordId: MedicalRecordId,
      dateOfScheduling: Instant
  )

}
