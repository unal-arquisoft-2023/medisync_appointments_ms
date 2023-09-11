package com.medisync.quickstart
import java.time.LocalTime
import monix.newtypes._
import monix.newtypes.integrations.DerivedCirceCodec
import NewtypesDoobie._
import NewtypesHttp4s._

object Doctors {
  type DoctorAvailabilityId = DoctorAvailabilityId.Type
  object DoctorAvailabilityId
      extends NewsubtypeWrapped[Int]
      with DerivedCirceCodec
      with DerivedHttp4sParamCodec

  type DoctorId = DoctorId.Type
  object DoctorId
      extends NewtypeWrapped[Int]
      with DerivedCirceCodec
      with DerivedDoobieCodec
      with DerivedHttp4sParamCodec

  type SpecialtyId = SpecialtyId.Type
  object SpecialtyId
      extends NewtypeWrapped[Int]
      with DerivedCirceCodec
      with DerivedDoobieCodec
      with DerivedHttp4sParamCodec

  case class DaySchedule(from: LocalTime, to: LocalTime)
  case class WeekSchedule(
      monday: Option[DaySchedule],
      tuesday: Option[DaySchedule],
      wednesday: Option[DaySchedule],
      thursday: Option[DaySchedule],
      friday: Option[DaySchedule],
      saturday: Option[DaySchedule],
      sunday: Option[DaySchedule]
  )
  case class DoctorAvailability(
      id: DoctorAvailabilityId,
      doctorId: DoctorId,
      specialtyId: SpecialtyId,
      schedule: WeekSchedule
  )
}
