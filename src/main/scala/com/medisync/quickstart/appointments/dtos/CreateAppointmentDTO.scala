package com.medisync.quickstart.appointment

import com.medisync.quickstart.domain.Appointments._
import com.medisync.quickstart.domain.Doctors._
import java.time.Instant
import io.circe.{Decoder, Encoder}
import org.http4s.EntityDecoder
import org.http4s.circe._
import cats.effect.Concurrent
import java.time.LocalDate

final case class CreateAppointmentDTO(
    patientId: PatientId,
    doctorId: DoctorId,
    date: LocalDate,
    specialty: Specialty,
    blockId: BlockId
)

object CreateAppointmentDTO:
  given JsonDecoderCreateAppointmentDTO: Decoder[CreateAppointmentDTO] =
    Decoder.forProduct5[
      CreateAppointmentDTO,
      PatientId,
      DoctorId,
      LocalDate,
      Specialty,
      BlockId
    ]("patient_id", "doctor_id", "date", "specialty", "block_id")(
      CreateAppointmentDTO.apply
    )
  given EntityDecoderCreateAppointmentDTO[F[_]: Concurrent]
      : EntityDecoder[F, CreateAppointmentDTO] = jsonOf[F, CreateAppointmentDTO]
