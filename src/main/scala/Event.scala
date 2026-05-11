import java.time.{LocalDate, LocalTime, Duration}

case class Event(
    name: String,
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime,
    category: String,
    location: Option[String] = None,
    notes: Option[String] = None,
    reminderMinutesBefore: Option[Int] = None  
) {
  def duration: Duration = Duration.between(startTime, endTime)
}
