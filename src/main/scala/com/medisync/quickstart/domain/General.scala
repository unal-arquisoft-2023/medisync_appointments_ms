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

  given decodeInstant: Decoder[Instant] = Decoder.decodeString.emapTry { str =>
    Try(Instant.parse(str))
  }
