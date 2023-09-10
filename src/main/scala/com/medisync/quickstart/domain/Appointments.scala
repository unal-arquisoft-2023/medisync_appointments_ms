import java.time.Instant

object Appointments {

    case class Appointment(
        id: Int,
        date: Instant,
        doctorId: Int,
        patientId: Int,
        medicalRecordId: Int,
        dateOfScheduling: Instant 
    )

}