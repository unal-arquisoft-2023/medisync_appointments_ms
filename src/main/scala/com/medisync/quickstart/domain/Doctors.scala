package com.medisync.quickstart
import java.time.LocalTime
import monix.newtypes._
import monix.newtypes.integrations.DerivedCirceCodec
import NewtypesDoobie._
import NewtypesHttp4s._
import General._
import io.circe._
import io.circe.syntax._
import scala.util.Try
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._

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

  enum Specialty(val str: String):
    case GeneralMedicine extends Specialty("General Medicine")
    case Pediatrics extends Specialty("Pediatrics")
    case Cardiology extends Specialty("Cardiology")
    case Orthopedics extends Specialty("Orthopedics")
    case Dermatology extends Specialty("Dermatology")
    case Gastroenterology extends Specialty("Gastroenterology")
    case Neurology extends Specialty("Neurology")
    case Ophthalmology extends Specialty("Ophthalmology")
    case Oncology extends Specialty("Oncology")
    case Otolaryngology extends Specialty("Otolaryngology")
    case Urology extends Specialty("Urology")
    case Psychiatry extends Specialty("Psychiatry")
    case Obstetrics extends Specialty("Obstetrics")
    case Gynecology extends Specialty("Gynecology")
    case Anesthesiology extends Specialty("Anesthesiology")
    case Radiology extends Specialty("Radiology")
    case Pathology extends Specialty("Pathology")
    case Emergency extends Specialty("Emergency")
    case FamilyMedicine extends Specialty("Family Medicine")
    case InternalMedicine extends Specialty("Internal Medicine")
    case Surgery extends Specialty("Surgery")
    case Other extends Specialty("Other")

    override def toString(): String = str


  object Specialty:
    def unapply(value: String) = Try(Specialty.valueOf(value)).toOption
    given Encoder[Specialty] = Encoder.encodeString.contramap[Specialty](_.toString())
    given Decoder[Specialty] = Decoder.decodeString.emapTry[Specialty](v => Try(Specialty.valueOf(v)))
    given Meta[Specialty] = pgEnumStringOpt("specialty_enum", v => Try(Specialty.valueOf(v)).toOption, _.toString())


  case class WeekSchedule(
      monday: Option[TimeRange],
      tuesday: Option[TimeRange],
      wednesday: Option[TimeRange],
      thursday: Option[TimeRange],
      friday: Option[TimeRange],
      saturday: Option[TimeRange],
      sunday: Option[TimeRange]
  )
  case class DoctorAvailability(
      id: DoctorAvailabilityId,
      doctorId: DoctorId,
      specialtyId: Specialty,
      schedule: WeekSchedule
  )

}
