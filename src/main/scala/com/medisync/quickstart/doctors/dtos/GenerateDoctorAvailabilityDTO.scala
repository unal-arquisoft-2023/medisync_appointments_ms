package com.medisync.quickstart

import Doctors._
import java.time.Instant
import io.circe.{Decoder, Encoder}
import org.http4s.EntityDecoder
import org.http4s.circe._
import cats.effect.Concurrent
import com.medisync.quickstart.General.TimeRange

final case class GenerateDoctorAvailabilityDTO(
    doctorId: DoctorId,
    specialty: Specialty
)

object GenerateDoctorAvailabilityDTO:
  given JsonDecoderGenerateDoctorAvailabilityDTO: Decoder[GenerateDoctorAvailabilityDTO] =
    Decoder.forProduct2[GenerateDoctorAvailabilityDTO, DoctorId, Specialty]("doctor_id", "specialty")(
      GenerateDoctorAvailabilityDTO.apply
    )
  given EntityDecoderGenerateDoctorAvailabilityDTO[F[_]: Concurrent]
      : EntityDecoder[F, GenerateDoctorAvailabilityDTO] = jsonOf[F, GenerateDoctorAvailabilityDTO]
