package com.medisync.quickstart.outside

import cats.effect.Concurrent
import com.medisync.quickstart.domain.Appointments._

trait Gateway[F[_]: Concurrent] {
  def apply[F[_]](implicit ev: Gateway[F]): Gateway[F] = ev
  def createMedicalRecord: F[MedicalRecordId]
  def updateMedicalRecord(medRecId: MedicalRecordId, comment: String): F[Boolean]
  def deleteMedicaRecord(medRecId: MedicalRecordId): F[Boolean]

  def createNotification(appointment: Appointment): F[Boolean]
  def deleteNotification(appointmentId: AppointmentId): F[Boolean]
}
