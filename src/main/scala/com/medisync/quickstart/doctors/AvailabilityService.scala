package com.medisync.quickstart.availability

import com.medisync.quickstart.domain.Appointments._
import com.medisync.quickstart.domain.Doctors._
import com.medisync.quickstart.utilities.TimeIntervals._
import java.time.LocalDate

import cats.effect.Async
import cats.syntax.all._


trait AvailabilityService[F[_]]:
  def createOrUpdate(docId: DoctorId, spec: Specialty, day: LocalDate, times: LocalTimeMultiInterval ): F[Unit]
  def canBook(docId: DoctorId, spe: Specialty, date: LocalDate, blockId: BlockId): F[Boolean]
  def getDayAvailability(spe: Specialty, date: LocalDate): F[Map[DoctorId,DoctorAvailability]]


object AvailabilityService:
  def apply[F[_]](implicit ev: AvailabilityService[F]): AvailabilityService[F] = ev

  def impl[F[_]: Async](avRep: AvailabilityRepository[F]) =
    new AvailabilityService[F]:

      def createOrUpdate(docId: DoctorId, spec: Specialty, day: LocalDate, times: LocalTimeMultiInterval): F[Unit] =
        avRep.changeOrUpdateAvailability(docId,spec,day,times) 

      def canBook(docId: DoctorId, spe: Specialty, date: LocalDate, blockId: BlockId): F[Boolean] = 
        for {
          res <- avRep.hasAppointment(docId,spe,date,blockId)
        } yield ! res

      def getDayAvailability(spe: Specialty, date: LocalDate): F[Map[DoctorId, DoctorAvailability]] = 
        avRep.findManyWithSpecialtyInDay(spe,date)
