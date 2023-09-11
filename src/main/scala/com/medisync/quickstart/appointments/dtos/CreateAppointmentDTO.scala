package com.medisync.quickstart

import Appointments._
import Doctors._
import java.time.Instant
import io.circe.{Decoder, Encoder}
import org.http4s.EntityDecoder
import org.http4s.circe._
import cats.effect.Concurrent

final case class CreateAppointmentDTO(
    patientId: PatientId,
    doctorId: DoctorId,
    date: Instant
)

object CreateAppointmentDTO:
  given JsonDecoderCreateAppointmentDTO: Decoder[CreateAppointmentDTO] =
    Decoder.forProduct3("patient_id", "doctor_id", "date")(
      CreateAppointmentDTO.apply
    )
  given EntityDecoderCreateAppointmentDTO[F[_]: Concurrent]
      : EntityDecoder[F, CreateAppointmentDTO] = jsonOf[F, CreateAppointmentDTO]
