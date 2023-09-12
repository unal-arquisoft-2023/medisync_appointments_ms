package com.medisync.quickstart

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.{Request, Response}
import org.http4s.Status
import org.http4s.QueryParamDecoder
import org.http4s.QueryParamCodec
import org.http4s.dsl.impl.+&
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import java.time.Instant
import java.time.format.DateTimeFormatter
import org.http4s.circe._
import cats.effect.Concurrent
import cats.implicits._
import cats.syntax._
import io.circe._
import io.circe.syntax._
import io.circe.literal._
import GenerateDoctorAvailabilityDTO._
import Doctors._
import Doctors.DoctorAvailability._
import com.medisync.quickstart.Doctors.DoctorId
import NewtypesRouteVar.Var
import cats.data.EitherT
import cats.data.OptionT

object DoctorController:

  def apply[F[_]: Concurrent](apService: DoctorService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    import Specialty._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "doctor" =>
        for {
          dto <- req.as[GenerateDoctorAvailabilityDTO]
          apId <- apService.generate(dto.doctorId, dto.specialty)
          res <- Status.Created(json"""{"doctor_availability_id": $apId}""")
        } yield res

      case GET -> Root / "doctor" / Var[Specialty](spe) / Var[DoctorId](
            docId
          ) =>
        for {
          docAvOp <- apService.findOne(docId, spe)
          res <- docAvOp.map(x => Ok(x.asJson)).getOrElse(Status.NotFound())
        } yield res

      case req @ POST -> Root / "doctor" / Var[Specialty](spe) / Var[DoctorId](
            docId
          ) =>
        for {
          docAvOp <- apService.findOne(docId, spe)
          dto <- req.as[UpdateDoctorAvailabilityDTO]
          res <- docAvOp
            .map(v => apService.update(v.id, dto.day, dto.time) >> Ok())
            .getOrElse(Status.NotFound())
        } yield res

    }
