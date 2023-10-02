package com.medisync.quickstart.availability

import com.medisync.quickstart.utilities.TimeIntervals._
import com.medisync.quickstart.utilities.TimeIntervals.given
import java.time.LocalDate
import io.circe.Decoder
import org.http4s.EntityDecoder
import org.http4s.circe._
import cats.effect.Concurrent
import ch.qos.logback.core.encoder.Encoder

final case class CreateDoctorAvailabilityDTO(
    date: LocalDate,
    times: LocalTimeMultiInterval
)

object CreateDoctorAvailabilityDTO:
  given Decoder[CreateDoctorAvailabilityDTO] = Decoder.forProduct2[
    CreateDoctorAvailabilityDTO,
    LocalDate,
    LocalTimeMultiInterval
  ]("date", "times")(CreateDoctorAvailabilityDTO.apply)


  given EntityDecoderDTO[F[_]: Concurrent]: EntityDecoder[F,CreateDoctorAvailabilityDTO] = 
    jsonOf[F,CreateDoctorAvailabilityDTO]

  