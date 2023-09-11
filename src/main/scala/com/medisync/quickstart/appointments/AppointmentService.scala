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
import outside.Gateway
import com.medisync.quickstart.Appointments._
import NewtypesDoobie._
import com.medisync.quickstart.Doctors._

trait AppointmentService[F[_]]:
    def create(patId: PatientId, docId: DoctorId, date: Instant): F[AppointmentId]

object AppointmentService:
    def apply[F[_]](implicit ev: AppointmentService[F]): AppointmentService[F] = ev


    def impl[F[_]: Concurrent](T: Transactor[F], C: Gateway[F]) = new AppointmentService[F]: 
        val dsl = new Http4sClientDsl[F]{}
        import dsl._
        def create(patId: PatientId, docId: DoctorId, date: Instant): F[AppointmentId] = 
            for {
                medRecId <- C.createMedicalRecord
                apId <- sql"INSERT INTO appointment (doctor_id,patient_id,medical_record_id,date) VALUES ($docId,$patId,$medRecId,$date)"
                            .update 
                            .withUniqueGeneratedKeys[AppointmentId]("id")
                            .transact(T)
            } yield apId


