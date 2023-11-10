package com.medisync.quickstart.appointment

import com.medisync.quickstart.domain.Doctors._
import com.medisync.quickstart.domain.Appointments._
import java.time.LocalDate
import cats.syntax.all._
import cats.effect.kernel.Async
import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

trait AppointmentRepository[F[_]]:
  def bookAppointment(
      patId: PatientId,
      docId: DoctorId,
      specialty: Specialty,
      date: LocalDate,
      blockId: BlockId,
      medRecId: MedicalRecordId
  ): F[AppointmentRecord]

  def cancelAppointment(
      id: AppointmentId
  ): F[Boolean]

  def attendAppointment(
      id: AppointmentId
  ): F[Boolean]

  def getManyFromPatient(id: PatientId): F[List[AppointmentRecord]]
  def getManyFromDoctor(id: DoctorId): F[List[AppointmentRecord]]
  def getManyAvailableFromSpecialty(spe: Specialty): F[List[AvailableAppointment]]
  def findOne(id: AppointmentId): F[Option[AppointmentRecord]]

object AppointmentRepository:

  def impl[F[_]: Async](T: Transactor[F]) =
    new AppointmentRepository[F]:
      def bookAppointment(
          patId: PatientId,
          docId: DoctorId,
          specialty: Specialty,
          date: LocalDate,
          blockId: BlockId,
          medRecId: MedicalRecordId
      ): F[AppointmentRecord] =
        val insert = sql"""
          INSERT INTO appointment(
            appointment_date,
            block_time_id,
            doctor_id,
            patient_id,
            medical_record_id,
            notification_status,
            appointment_status,
            specialty
          )
          VALUES (
            ${date},
            ${blockId},
            ${docId},
            ${patId},
            ${medRecId},
            ${NotificationStatus.ToNotify},
            ${AppointmentStatus.Pending},
            ${specialty}
          )
          """.update.withUniqueGeneratedKeys[AppointmentRecord](
          "id",
          "appointment_date",
          "block_time_id",
          "doctor_id",
          "patient_id",
          "medical_record_id",
          "scheduled_timestamp",
          "appointment_status",
          "notification_status",
          "specialty"
        )

        for {
          ap <- insert.transact(T)
        } yield ap

      def cancelAppointment(id: AppointmentId): F[Boolean] =
        val update = sql"""
          UPDATE appointment 
          SET appointment_status = ${AppointmentStatus.Canceled}
          WHERE id = ${id}
        """
        for {
          r <- update.update.run.transact(T)
        } yield r > 0

      def attendAppointment(id: AppointmentId): F[Boolean] =
        val update = sql"""
          UPDATE appointment 
          SET appointment_status = ${AppointmentStatus.Attended}
          WHERE id = ${id}
        """
        for {
          r <- update.update.run.transact(T)
        } yield r > 0

      def getManyFromPatient(id: PatientId): F[List[AppointmentRecord]] =
        val select = sql"""
          SELECT
            id,
            appointment_date,
            block_time_id,
            doctor_id,
            patient_id,
            medical_record_id,
            scheduled_timestamp,
            appointment_status,
            notification_status,
            specialty
          FROM appointment
          WHERE patient_id = ${id}
        """
        for {
          ap <- select.query[AppointmentRecord].to[List].transact(T)
        } yield ap


      
      def getManyFromDoctor(id: DoctorId): F[List[AppointmentRecord]] =
        val select = sql"""
          SELECT
            id,
            appointment_date,
            block_time_id,
            doctor_id,
            patient_id,
            medical_record_id,
            scheduled_timestamp,
            appointment_status,
            notification_status,
            specialty
          FROM appointment
          WHERE doctor_id = ${id}
        """
        for {
          ap <- select.query[AppointmentRecord].to[List].transact(T)
        } yield ap


      def getManyAvailableFromSpecialty(spe: Specialty): F[List[AvailableAppointment]] = 
        val select = sql"""
          SELECT
            availability_date,
            block_time_id,
            doctor_id,
            start_time_block,
            end_time_block
          FROM general_schedule
          WHERE appointment_id IS NULL
          AND specialty = ${spe}
        """
        for {
          ap <- select.query[AvailableAppointment].to[List].transact(T)
        } yield ap


      def findOne(id: AppointmentId): F[Option[AppointmentRecord]] =
        val select = sql"""
          SELECT
            id,
            appointment_date,
            block_time_id,
            doctor_id,
            patient_id,
            medical_record_id,
            scheduled_timestamp,
            appointment_status,
            notification_status,
            specialty
          FROM appointment
          WHERE id = ${id}
        """
        for {
          ap <- select.query[AppointmentRecord].option.transact(T)
        } yield ap



