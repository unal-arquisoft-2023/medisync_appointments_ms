package com.medisync.quickstart.domain
import monix.newtypes._
import monix.newtypes.integrations.DerivedCirceCodec
import com.medisync.quickstart.utilities.NewtypesRouteVar._
import com.medisync.quickstart.utilities.NewtypesDoobie._
import com.medisync.quickstart.utilities.NewtypesHttp4s._
import io.circe._
import io.circe.syntax._
import scala.util.Try
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.enumerated.JdbcType.Time
import java.time.LocalDate
import java.time.LocalTime
import com.medisync.quickstart.utilities.{
  NewtypesDoobie,
  NewtypesHttp4s,
  NewtypesRouteVar
}
import com.medisync.quickstart.utilities.TimeIntervals._
import com.medisync.quickstart.utilities.TimeIntervals.given
import com.medisync.quickstart.domain.Appointments.BlockId

object Doctors:

  type DoctorId = DoctorId.Type
  object DoctorId
      extends NewtypeWrapped[Int]
      with DerivedCirceCodec
      with DerivedDoobieCodec
      with DerivedHttp4sParamCodec
  given KeyEncoder[DoctorId] = (key: DoctorId) => key.toString()

  type DoctorAvailabilityId = DoctorAvailabilityId.Type
  object DoctorAvailabilityId
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
    given RouteUnapplicable[Specialty] = new RouteUnapplicable[Specialty]:
      def unapply(value: String) = Try(Specialty.valueOf(value)).toOption

    given Encoder[Specialty] =
      Encoder.encodeString.contramap[Specialty](_.toString())

    given Decoder[Specialty] =
      Decoder.decodeString.emapTry[Specialty](v => Try(Specialty.valueOf(v)))

    given Meta[Specialty] = pgEnumStringOpt(
      "specialty_enum",
      v => Try(Specialty.valueOf(v)).toOption,
      _.toString()
    )

  case class TimeBlock(
      id: BlockId,
      startTime: LocalTime,
      endTime: LocalTime
  )

  given Encoder[TimeBlock] = new Encoder[TimeBlock]:
    def apply(a: TimeBlock): Json = 
      Json.obj(
        ("id", a.id.asJson),
        ("start", a.startTime.asJson),
        ("end", a.endTime.asJson)
      )

  given Read[TimeBlock] =
    Read[(BlockId, LocalTime, LocalTime)].map { case (id, st, et) =>
      TimeBlock(id, st, et)
    }

  case class DoctorAvailability(
      doctorId: DoctorId,
      specialty: Specialty,
      date: LocalDate,
      times: List[TimeBlock]
  )

  given Encoder[DoctorAvailability] = new Encoder[DoctorAvailability]:
    def apply(a: DoctorAvailability): Json = 
      Json.obj(
        ("doctorId", a.doctorId.asJson),
        ("specialty", a.specialty.asJson),
        ("date", a.date.asJson),
        ("times", a.times.asJson)
      )

  case class DoctorAvailabilityRegister(
      id: DoctorAvailabilityId,
      doctorId: DoctorId,
      specialty: Specialty,
      date: LocalDate,
      times: LocalTimeInterval
  )


  given Read[DoctorAvailabilityRegister] =
    Read[
      (
          DoctorAvailabilityId,
          DoctorId,
          Specialty,
          LocalDate,
          LocalTime,
          LocalTime
      )
    ].map { case (id, docId, spe, date, startTime, endTime) =>
      DoctorAvailabilityRegister(
        id,
        docId,
        spe,
        date,
        LocalTimeInterval.create(startTime, endTime)
      )
    }
