package com.medisync.quickstart.availability

import com.medisync.quickstart.domain.Doctors.DoctorId
import com.medisync.quickstart.domain.Doctors.Specialty
import java.time.LocalDate
import com.medisync.quickstart.utilities.TimeIntervals._
import com.medisync.quickstart.utilities.TimeIntervals.given
import com.medisync.quickstart.domain.Doctors._
import com.medisync.quickstart.domain.Doctors.given
import javax.print.Doc
import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.syntax.all._
import doobie.util.transactor.Transactor
import spire.math.extras.interval.IntervalSeq
import java.time.LocalTime
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres.implicits._
import com.medisync.quickstart.domain.Appointments.BlockId

trait AvailabilityRepository[F[_]]:
  def changeOrUpdateAvailability(
      docId: DoctorId,
      spe: Specialty,
      date: LocalDate,
      times: LocalTimeMultiInterval
  ): F[Unit]
  def findManyWithSpecialtyInDay(
      spe: Specialty,
      day: LocalDate
  ): F[Map[DoctorId, DoctorAvailability]]
  def hasAppointment(
      docId: DoctorId,
      specialty: Specialty,
      date: LocalDate,
      blockId: BlockId
  ): F[Boolean]

object AvailabilityRepository:
  def apply[F[_]](implicit avRep: AvailabilityRepository[F]) = avRep

  def impl[F[_]: Async](T: Transactor[F]) =
    new AvailabilityRepository[F]:
      def changeOrUpdateAvailability(
          docId: DoctorId,
          spe: Specialty,
          date: LocalDate,
          newTimes: LocalTimeMultiInterval
      ): F[Unit] =

        val doc_exists =
          sql"""
           SELECT COUNT(*) 
           FROM doctor_availability 
           WHERE doctor_id = ${docId} 
           AND specialty = ${spe} 
           AND availability_date = ${date}
          """
            .query[Int]
            .unique
            .transact(T)
            .map(_ > 0)

        def block(in: LocalTimeInterval) =
          (fr"SELECT id,start_time_block,end_time_block FROM availability_time_block "
            ++ fr"WHERE start_time_block >= ${in.lower} AND end_time_block <= ${in.upper}")
            .query[(BlockId, LocalTime, LocalTime)]
            .to[List]
            .transact(T)

        val select =
          sql"SELECT id,doctor_id,specialty,availability_date,start_time,end_time FROM doctor_availability " ++
            fr"WHERE doctor_id = ${docId} AND specialty = ${spe} AND availability_date = ${date} "

        val delete =
          sql"DELETE FROM doctor_availability " ++
            fr"WHERE doctor_id = ${docId} AND specialty = ${spe} AND availability_date = ${date} "

        def insert(in: LocalTimeInterval) =
          (sql"INSERT INTO doctor_availability (doctor_id,specialty,availability_date,start_time,end_time) " ++
            fr"VALUES (${docId}, ${spe}, ${date}, ${in.lower}, ${in.upper}) ").update.run
            .transact(T)

        def handleExisting =
          for {
            r <- select.query[DoctorAvailabilityRegister].to[List].transact(T)
            originalMultiInterval = r
              .map(_.times)
              .foldLeft[LocalTimeMultiInterval](LocalTimeMultiInterval.empty)(
                _ | _
              )
            removedMultiInterval = newTimes ^ originalMultiInterval
            blocksToRemove <- removedMultiInterval.intervals.toList.traverse(
              block
            ) map (_.flatten)

            verifiedBlocks <- blocksToRemove.traverse { case (id, start, end) =>
              this.canRemoveFromAvailability(docId, spe, date, id) map ((
                _,
                id,
                start,
                end
              ))
            }

            conflict = (!verifiedBlocks.foldLeft(true)(_ & _._1))
            conflictMessage =
              if conflict then
                "Cannot modifi availability: \n" +
                  verifiedBlocks
                    .filter(!_._1)
                    .map(v =>
                      s"Appoitnment at time block ${v._2} (${v._3} - ${v._4})"
                    )
                    .mkString("\n")
              else ""

            _ <- Async[F].raiseWhen(conflict)(new Exception(conflictMessage))

            _ <- delete.update.run.transact(T)

          } yield ()

        for {
          exists <- doc_exists
          _ <- if exists then handleExisting else Async[F].unit

          _ <- newTimes.intervals.toList.traverse(insert)

        } yield () 

      def findManyWithSpecialtyInDay(
          spe: Specialty,
          day: LocalDate
      ): F[Map[DoctorId, DoctorAvailability]] =
        val select = sql"""
        SELECT 
          doctor_id,
          specialty, 
          availability_date, 
          block_time_id, 
          start_time_block, 
          end_time_block 
        FROM general_schedule 
        WHERE specialty = ${spe}
        AND availability_date = ${day}
        AND appointment_id IS NULL
        """
        for {
          reg <- select
            .query[(DoctorId, Specialty, LocalDate, TimeBlock)]
            .to[List]
            .transact(T)
          res = reg.foldLeft[Map[DoctorId, DoctorAvailability]](
            collection.immutable.Map[DoctorId, DoctorAvailability]()
          ) { (map, r) =>
            r match {
              case (docId, spe, date, block) =>
                val newAv = map
                  .get(docId)
                  .map(v =>
                    DoctorAvailability(
                      v.doctorId,
                      v.specialty,
                      v.date,
                      block +: v.times
                    )
                  )
                  .getOrElse(
                    DoctorAvailability(docId, spe, date, List(block))
                  )

                if map.contains(docId) then 
                  map.updated(docId, newAv)
                else 
                  map + (docId -> newAv)
                

            }

          }
        } yield res 

      def canRemoveFromAvailability(
          docId: DoctorId,
          specialty: Specialty,
          date: LocalDate,
          blockId: BlockId
      ): F[Boolean] =
        val select =
          sql"SELECT COUNT(*) FROM appointment "
            ++ fr"WHERE doctor_id = ${docId} "
            ++ fr"AND specialty = ${specialty} "
            ++ fr"AND block_time_id = ${blockId} "

        for {
          n <- select.query[Int].unique.transact(T)
        } yield n == 0

      def hasAppointment(
          docId: DoctorId,
          specialty: Specialty,
          date: LocalDate,
          blockId: BlockId
      ): F[Boolean] =
        val select = sql"SELECT COUNT(*) FROM general_schedule "
          ++ fr"WHERE doctor_id = ${docId} "
          ++ fr"AND appointment_status is NULL "
          ++ fr"AND specialty= ${specialty} "
          ++ fr"AND availability_date = ${date} "
          ++ fr"AND block_time_id = ${blockId} "

        for {
          n <- select.query[Int].unique.transact(T)
        } yield n == 0
