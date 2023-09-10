package com.medisync.quickstart

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.QueryParamDecoder
import org.http4s.QueryParamCodec
import java.time.Instant
import java.time.format.DateTimeFormatter
import org.http4s.dsl.impl.+&
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import cats.effect.Sync
import cats.implicits._

object AppointmentController:
  import Appointments.Appointment

  implicit val isoInstantCodec: QueryParamCodec[Instant] =
    QueryParamCodec.instantQueryParamCodec(DateTimeFormatter.ISO_INSTANT)

  object IsoInstantParamMatcher
      extends QueryParamDecoderMatcher[Instant]("date")

  object PatientIdQueryParam extends QueryParamDecoderMatcher[Int]("patient_id")
  object DoctorIdQueryParam extends QueryParamDecoderMatcher[Int]("doctor_id")

  def apply[F[_]: Sync](apService: AppointmentService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    import AppointmentService._
    HttpRoutes.of[F] {
      case GET -> Root / "appointment" :? PatientIdQueryParam(
            patId
          ) +& DoctorIdQueryParam(docId) +& IsoInstantParamMatcher(date) =>
        for {
          app <- apService.create(patId,docId,date)
          resp <- Ok(app)
        } yield resp

    }
