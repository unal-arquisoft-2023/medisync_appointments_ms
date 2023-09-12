package com.medisync.quickstart

import Doctors._
import java.time.Instant
import io.circe.{Decoder, Encoder}
import org.http4s.EntityDecoder
import org.http4s.circe._
import cats.effect.Concurrent
import com.medisync.quickstart.General.TimeRange
import com.medisync.quickstart.General.DayOfWeek
import doobie.enumerated.JdbcType.Time

final case class UpdateDoctorAvailabilityDTO(
    day:DayOfWeek,
    time: TimeRange
)

object UpdateDoctorAvailabilityDTO:
  given Decoder[UpdateDoctorAvailabilityDTO] =
    Decoder.forProduct2[UpdateDoctorAvailabilityDTO,  DayOfWeek, TimeRange]( "day","time")(
      UpdateDoctorAvailabilityDTO.apply
    )
  given [F[_]: Concurrent] : EntityDecoder[F, UpdateDoctorAvailabilityDTO] = jsonOf[F, UpdateDoctorAvailabilityDTO]
