package com.medisync.quickstart

import java.time.Instant
import Appointments.Appointment
import cats.effect.Concurrent
import cats.implicits._
import io.circe.{Encoder,Decoder}
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._
import org.http4s.Method._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.postgres.pgisimplicits._

trait AppointmentService[F[_]]:
    def create(patId: Int, docId: Int, date: Instant): F[Appointment]


given Decoder[Appointment] = Decoder.derived[Appointment]
given [F[_]: Concurrent]: EntityDecoder[F, Appointment] = jsonOf
given Encoder[Appointment] = Encoder.AsObject.derived[Appointment]
given [F[_]]: EntityEncoder[F, Appointment] = jsonEncoderOf

object AppointmentService:
    def apply[F[_]](implicit ev: AppointmentService[F]): AppointmentService[F] = ev


    def impl[F[_]: Concurrent](T: Transactor[F], C: Client[F]): AppointmentService[F] = new AppointmentService[F]: 
        val dsl = new Http4sClientDsl[F]{}
        import dsl._
        def create(patId: Int, docId: Int, date: Instant): F[Appointment] = 
                // TODO: bring gateway and request for medical_record before creating appointment
                println(s"craeting $patId, $docId, $date")
                sql"INSERT INTO appointment (doctor_id,patient_id,medical_record_id,date) VALUES ($docId,$patId,${1},$date)"
                    .update
                    .withUniqueGeneratedKeys[Int]("id")
                    .map[Appointment](Appointment(_,date,-1,-1,-1,date))
                    .transact(T)
                    // .attemptSomeSqlState {
                    //     case x => println(x)
                    // }
                    // .map(_ => Appointment(-1,date,-1,-1,-1,date))


