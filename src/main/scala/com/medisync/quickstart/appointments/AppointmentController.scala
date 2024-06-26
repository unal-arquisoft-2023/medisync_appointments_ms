package com.medisync.quickstart.appointment

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.{Request, Response}
import org.http4s.Status
import org.http4s.QueryParamDecoder
import org.http4s.QueryParamCodec
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import java.time.Instant
import java.time.format.DateTimeFormatter
import org.http4s.circe._
import cats.effect.Concurrent
import cats.implicits._
import io.circe._
import io.circe.syntax._
import io.circe.literal._
import com.medisync.quickstart.domain.Appointments._
import com.medisync.quickstart.domain.Doctors._
import com.medisync.quickstart.utilities.NewtypesRouteVar.Var
import cats.data.EitherT
import com.medisync.quickstart.utilities.NewtypesRouteVar
import org.http4s.ParseFailure

object AppointmentController:

  implicit val isoInstantCodec: QueryParamCodec[Instant] =
    QueryParamCodec.instantQueryParamCodec(DateTimeFormatter.ISO_INSTANT)

  object IsoInstantParamMatcher
      extends QueryParamDecoderMatcher[Instant]("date")

  object PatientIdQueryParam
      extends QueryParamDecoderMatcher[PatientId]("patient_id")
  object DoctorIdQueryParam
      extends QueryParamDecoderMatcher[DoctorId]("doctor_id")

  implicit val specialtyQueryParamDecoder: QueryParamDecoder[Specialty] =
    QueryParamDecoder[String].emap(Specialty.fromString(_).toRight(ParseFailure("Invalid specialty", "Invalid specialty")))

  object SpecialtyQueryParam
      extends QueryParamDecoderMatcher[Specialty]("specialty")

  def apply[F[_]: Concurrent](apService: AppointmentService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "appointment" => 
        for {
          dto <- req.as[CreateAppointmentDTO]
          _ = println(dto)
          apId <- apService.create(dto.patientId, dto.doctorId, dto.date, dto.specialty, dto.blockId)
          res <- Status.Created(json"""{"appointment_id": $apId}""")
        } yield res

      case GET -> Root / "appointment" / Var[AppointmentId](appId) =>
        for {
          app <- apService.findOne(appId)
          res <- (app).map(x => Ok(x.asJson)).getOrElse(Status.NotFound())
        } yield res

      case POST -> Root / "appointment" / Var[AppointmentId](
            appId
          ) / "cancel" =>
        def check = (s: AppointmentStatus) =>
          s match {
            case AppointmentStatus.Canceled =>
              Status.Conflict("Cannot cancel an already canceled appointment").asLeft
            case AppointmentStatus.Attended =>
              Status
                .Conflict("Cannot attend a canceled appointment")
                .asLeft
            case AppointmentStatus.Missed =>
              Status.Conflict("Cannot attend a missed appointment").asLeft
            case AppointmentStatus.Pending => Status.Ok().asRight
          }
        for {
          appOp <- apService.findOne(appId)

          statusVal <- EitherT
            .fromOption[F](appOp, Status.NotFound().asLeft[F[Response[F]]])
            .map(ap => check(ap.status))
            .merge
          _ <-
            if statusVal.isRight then apService.cancel(appId) else false.pure[F]
          res <- statusVal.merge
        } yield res

      case POST -> Root / "appointment" / Var[AppointmentId](
            appId
          ) / "attend" =>
        def check = (s: AppointmentStatus) =>
          s match {
            case AppointmentStatus.Canceled =>
              Status.Conflict("Cannot attend a canceled appointment").asLeft
            case AppointmentStatus.Attended =>
              Status
                .Conflict("Cannot attend an already attended appointment")
                .asLeft
            case AppointmentStatus.Missed =>
              Status.Conflict("Cannot attend a missed appointment").asLeft
            case AppointmentStatus.Pending => Status.Ok().asRight
          }
        for {
          appOp <- apService.findOne(appId)

          statusVal <- EitherT
            .fromOption[F](appOp, Status.NotFound().asLeft[F[Response[F]]])
            .map(ap => check(ap.status))
            .merge

          _ <-
            if statusVal.isRight then apService.attend(appId) else false.pure[F]
          res <- statusVal.merge
        } yield res

      case GET -> Root / "appointments" :? PatientIdQueryParam(
            patId
        ) =>

        for {
          apps <- apService.findAllByPatient(patId)
          res <- Ok(apps.asJson)
        } yield res

      case GET -> Root / "appointments"  :? SpecialtyQueryParam(
            spe
        ) =>

        for {
          apps <- apService.findAllBySpecialty(spe)
          res <- Ok(apps.asJson)
        } yield res         

    }
