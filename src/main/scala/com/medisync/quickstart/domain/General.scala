package com.medisync.quickstart

import cats.syntax._
import cats.implicits._
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
import java.time.Instant
import io.circe.Decoder.Result
import io.circe.DecodingFailure.Reason
import io.circe.DecodingFailure

object General:
  case class TimeRange(start: Instant, end: Instant)

  given Encoder[TimeRange] =
    Encoder.forProduct2("start", "end")(tr => (tr.start, tr.end))
  // given Decoder[TimeRange] = Decoder.forProduciforProduci[TimeRange, Instant, Instant]("start","end")((s,e) => TimeRange(s,e))
  given Decoder[TimeRange] = new Decoder[TimeRange]:
    def apply(c: HCursor): Result[TimeRange] =
      for {
        start <- c.downField("start").as[Instant]
        end <- c.downField("end").as[Instant]
        res <-
          if start.compareTo(end) < 0 then TimeRange(start, end).asRight
          else
            DecodingFailure(
              Reason.CustomReason("start time must be before end time"),
              c
            ).asLeft
      } yield res

  given encodeInstant: Encoder[Instant] =
    Encoder.encodeString.contramap[Instant](_.toString)

  given decodeInstant: Decoder[Instant] =
    Decoder.decodeString.emapTry(str => Try(Instant.parse(str)))

  given Read[TimeRange] = Read[(Instant,Instant)].map(TimeRange.apply)

  enum DayOfWeek(val v: Int):
    case Monday extends DayOfWeek(1)
    case Tuesday extends DayOfWeek(2)
    case Wednesday extends DayOfWeek(3)
    case Thursday extends DayOfWeek(4)
    case Friday extends DayOfWeek(5)
    case Saturday extends DayOfWeek(6)
    case Sunday extends DayOfWeek(7)

    def toInt: Int = v
    override def toString(): String = v match {
      case 1 => "monday"
      case 2 => "tuesday"
      case 3 => "wednesday"
      case 4 => "thursday"
      case 5 => "friday"
      case 6 => "saturday"
      case 7 => "sunday"
    }


  object DayOfWeek:
    def fromInt(v: Int): DayOfWeek = v match {
      case 1 => Monday
      case 2 => Tuesday
      case 3 => Wednesday
      case 4 => Thursday
      case 5 => Friday
      case 6 => Saturday
      case 7 => Sunday
      case _ =>
        throw java.lang.IllegalArgumentException(
          "Day should be represented with a number from 1 to 7"
        )
    }
    given Encoder[DayOfWeek] = Encoder.encodeInt.contramap[DayOfWeek](_.toInt)
    given Decoder[DayOfWeek] =
      Decoder.decodeInt.emapTry[DayOfWeek](v => Try(DayOfWeek.fromInt(v)))