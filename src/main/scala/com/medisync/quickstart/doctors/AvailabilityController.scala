package com.medisync.quickstart.availability

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.{Request, Response}
import org.http4s.Status
import org.http4s.circe._
import cats.effect.Concurrent
import cats.implicits._
import io.circe._
import io.circe.parser.decode
import io.circe.syntax._
import com.medisync.quickstart.domain.Doctors._
import com.medisync.quickstart.utilities.NewtypesRouteVar._
import com.medisync.quickstart.utilities.NewtypesRouteVar
import com.medisync.quickstart.utilities.TimeIntervals._
import com.medisync.quickstart.domain.Appointments.BlockId

object AvailabilityController:

  def apply[F[_]: Concurrent](
      avService: AvailabilityService[F]
  ): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._

    import DoctorId.given
    HttpRoutes.of[F] {
      case req @ POST -> Root / "availability" / Var[DoctorId](docId) / Var[
            Specialty
          ](spe) =>
        val test = """{
      "date": "2023-10-01",
      "times": [
          {
              "start": "7:00:00",
              "end":"11:00:00"
          },
          {
              "start": "20:00:00",
              "end": "21:00:00"
          }
      ] 
  }
        """
        println(
          decode[CreateDoctorAvailabilityDTO](test)
          // "" + (
          // IntervalSeq.empty[LocalTime]
          // | LocalTimeInterval.create(LocalTime.of(10,0), LocalTime.of(12,0))
          // | LocalTimeInterval.create(LocalTime.of(13,0), LocalTime.of(13,20))
          // ).asJson
        )
        for {
          dto <- req.as[CreateDoctorAvailabilityDTO]
          _ <- avService.createOrUpdate(docId, spe, dto.date, dto.times)
          res <- Status.Created()
        } yield res

      case req @ GET -> Root / "availability" / Var[Specialty](
            spe
          ) / LocalDateVar(date) =>
        for {
          directory <- avService.getDayAvailability(spe, date)
          res <- Ok(directory.asJson)
        } yield res

      case GET -> Root / "availability" / "canBook" / Var[DoctorId](
            docId
          ) / Var[Specialty](spe) / LocalDateVar(date) / Var[BlockId](
            blockId
          ) =>
        for {
          canBook <- avService.canBook(docId, spe, date,blockId)
          res <- Ok(canBook.asJson)
        } yield res

    }
