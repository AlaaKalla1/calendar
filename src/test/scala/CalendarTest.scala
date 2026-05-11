import org.scalatest.funsuite.AnyFunSuite
import java.time.{LocalDate, LocalTime}

class CalendarTest extends AnyFunSuite:

  test("Calendar should add and list events correctly") {
    val calendar = new Calendar()
    val event = Event(
      name = "Birthday",
      date = LocalDate.of(2025, 5, 2),
      startTime = LocalTime.of(14, 0),
      endTime = LocalTime.of(16, 0),
      category = "personal"
    )

    calendar.addEvent(event)
    val events = calendar.listEvents(LocalDate.of(2025, 5, 2))

    assert(events.contains(event))
  }
