package com.medisync.quickstart

import Appointments._
import Doctors._
import java.time.Instant
import io.circe.{Decoder, Encoder}
import org.http4s.EntityDecoder
import org.http4s.circe._
import cats.effect.Concurrent
import com.medisync.quickstart.General.TimeRange

final case class CreateAppointmentDTO(
    patientId: PatientId,
    doctorId: DoctorId,
    date: TimeRange,
    specialty: Specialty
)

object CreateAppointmentDTO:
  given JsonDecoderCreateAppointmentDTO: Decoder[CreateAppointmentDTO] =
    Decoder.forProduct4[CreateAppointmentDTO, PatientId, DoctorId, TimeRange, Specialty]("patient_id", "doctor_id", "date","specialty")(
      CreateAppointmentDTO.apply
    )
  given EntityDecoderCreateAppointmentDTO[F[_]: Concurrent]
      : EntityDecoder[F, CreateAppointmentDTO] = jsonOf[F, CreateAppointmentDTO]
