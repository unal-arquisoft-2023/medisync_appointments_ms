package com.medisync.quickstart

import java.time.Instant
import Appointments.Appointment
import cats.effect.Async
import cats.implicits._
import io.circe.{Encoder, Decoder}
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
import General._
import com.medisync.quickstart.Doctors._
import java.time.format.DateTimeFormatter
import java.time.ZoneId

trait DoctorService[F[_]]:
  def generate(docId: DoctorId, spec: Specialty): F[DoctorAvailabilityId]
  def findOne(docId: DoctorId, spec: Specialty): F[Option[DoctorAvailability]]
  def update(docAvId: DoctorAvailabilityId, day: DayOfWeek, time: TimeRange): F[Boolean]
  def isAvailable(docId: DoctorId, spe: Specialty, day: DayOfWeek, time: TimeRange): F[Boolean]


object DoctorService:
  def apply[F[_]](implicit ev: DoctorService[F]): DoctorService[F] = ev

  def impl[F[_]: Async](T: Transactor[F], gw: Gateway[F]) =
    val daysList: List[String] = DayOfWeek.values.toList.map(_.toString()).map(v => s"lower($v), upper($v)")
    val otherValsList: List[String] ="doctor_id" +: "specialty" +: "enabled" +: List()
    val valsFr = fr"(" ++ (( "id" +: otherValsList )  ++ daysList).map(Fragment.const(_,None)).intercalate(fr",") ++ fr")"
    val othersFr = fr"(" ++ (("id" +: otherValsList).map(Fragment.const(_,None)).intercalate(fr",")) ++ fr")"
    val createValsFr =  fr"(" ++ (otherValsList.map(Fragment.const(_,None)).intercalate(fr",")) ++ fr")"
    val fullFr =  ((("id" +: otherValsList) ++ daysList).map(Fragment.const(_,None)).intercalate(fr",")) 
    new DoctorService[F]:
      val dsl = new Http4sClientDsl[F] {}
      import dsl._

      def generate(docId: DoctorId, spec:Specialty): F[DoctorAvailabilityId] = 
        val insert = fr"INSERT INTO doctor_availability" ++ createValsFr ++ fr" VALUES ($docId,$spec,${ true })"
        for {
          r <- insert.update.withUniqueGeneratedKeys[DoctorAvailabilityId]("id").transact(T)
        } yield r

      def findOne(docId: DoctorId, spec: Specialty): F[Option[DoctorAvailability]] = 
        val select = fr"SELECT " ++ fullFr ++ fr" FROM doctor_availability WHERE doctor_id = $docId AND specialty = $spec" 
        for {
          docAv <- select.query[DoctorAvailability].option.transact(T)
        } yield docAv


      def update(docAvId: DoctorAvailabilityId, day: DayOfWeek, time: TimeRange): F[Boolean] = 
        val dayStr = day.toString()
        val trString = TimeRange.toString()
        val update = sql"UPDATE doctor_availability SET monday = $trString::tstzrange WHERE id = $docAvId"
        for {
          r <- update.update.run.transact(T)
        } yield r > 0
      
      def isAvailable(docId: DoctorId, spe: Specialty, day: DayOfWeek, time: TimeRange): F[Boolean] = 
        val dayStr = day.toString()
        val trString = TimeRange.toString()
        val schQuery = sql"SELECT $trString::tstzrange @> $dayStr::tstzrange FROM doctor_availability WHERE doctor_id = $docId AND specialty=$spe"
        val appQuery = sql"SELECT bool_or($trString::tstzrange &> tstzrange(start_time, end_time)) WHERE doctor_id = $docId AND specialty = $spe"
        for {
          inSchedule <- schQuery.query[Boolean].unique.transact(T)
          not_available <- appQuery.query[Boolean].unique.transact(T)
        } yield inSchedule || !not_available
