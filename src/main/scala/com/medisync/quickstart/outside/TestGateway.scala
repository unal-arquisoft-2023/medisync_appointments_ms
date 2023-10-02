package com.medisync.quickstart.outside

import org.http4s.client.Client
import cats.effect.Concurrent
import cats.effect.implicits._
import java.time.Instant
import com.medisync.quickstart.domain.Appointments._
import scala.util.Random


object TestGateway:
    def impl[F[_]: Concurrent](client: Client[F]) = new Gateway[F]:
        var counter = 0 
        def createMedicalRecord = 
            Concurrent[F].pure({counter+= 1; MedicalRecordId(counter)})
        
        def updateMedicalRecord(medRecId: MedicalRecordId, comment: String): F[Boolean] = 
            Concurrent[F].pure(true)

        def deleteMedicaRecord(medRecId: MedicalRecordId): F[Boolean] = 
            Concurrent[F].pure(true)

        def createNotification(appointment: Appointment): F[Boolean] = 
            Concurrent[F].pure(math.random() < 0.5)
        
        def deleteNotification(appointmentId: AppointmentId): F[Boolean] = 
            Concurrent[F].pure(math.random() < 0.5)