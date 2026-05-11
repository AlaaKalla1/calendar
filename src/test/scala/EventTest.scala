import org.scalatest.funsuite.AnyFunSuite
import java.time.{LocalDate, LocalTime}

class EventTest extends AnyFunSuite:

  test("Event.duration should correctly calculate the difference between start and end time") {
    val event = Event(
      name = "Meeting",
      date = LocalDate.of(2025, 5, 1),
      startTime = LocalTime.of(10, 0),
      endTime = LocalTime.of(12, 0),
      category = "work"
    )

    assert(event.duration.toHours == 2)
  }
