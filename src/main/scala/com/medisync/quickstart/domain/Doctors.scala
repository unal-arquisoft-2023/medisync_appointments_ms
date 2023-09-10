import java.time.LocalTime
object Doctors {
  case class DaySchedule(from: LocalTime, to: LocalTime)
  case class WeekSchedule(
      monday: Option[DaySchedule],
      tuesday: Option[DaySchedule],
      wednesday: Option[DaySchedule],
      thursday: Option[DaySchedule],
      friday: Option[DaySchedule],
      saturday: Option[DaySchedule],
      sunday: Option[DaySchedule]
  )
  case class DoctorAvailability(
      id: Int,
      doctorId: Int,
      specialtyId: Int,
      schedule: WeekSchedule
  )
}
