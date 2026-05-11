import java.time.{LocalDate, LocalTime, Duration}
import scala.collection.mutable.Buffer
import java.io.{File, PrintWriter}
import scala.io.Source

class Calendar {
  private val events: Buffer[Event] = Buffer()

  def addEvent(event: Event): Unit = {
    events += event
    println(s"Event '${event.name}' added on ${event.date}.")
    saveToFile()
  }

  def removeEvent(eventName: String, date: LocalDate): Boolean = {
    val initialSize = events.size
    events --= events.filter(e => e.name == eventName && e.date == date)
    val changed = initialSize > events.size
    if (changed) saveToFile()
    changed
  }

  def listEvents(date: LocalDate): Vector[Event] = {
    events.filter(_.date == date).toVector
  }

  def saveToFile(filename: String = "events.txt"): Unit = {
    val writer = new PrintWriter(new File(filename))
    for (e <- events) {
      writer.println(s"${e.name}|${e.date}|${e.startTime}|${e.endTime}|${e.category}|${e.location.getOrElse("")}|${e.notes.getOrElse("")}")
    }
    writer.close()
  }
  def allEventsSorted: Vector[Event] = {
  events.sortBy(e => (e.date, e.startTime)).toVector
}


  def loadFromFile(filename: String = "events.txt"): Unit = {
    val file = new File(filename)
    if (file.exists()) {
      val lines = Source.fromFile(file).getLines()
      for (line <- lines) {
        val parts = line.split("\\|", -1)
        if (parts.length >= 5) {
          events += Event(
            name = parts(0),
            date = LocalDate.parse(parts(1)),
            startTime = LocalTime.parse(parts(2)),
            endTime = LocalTime.parse(parts(3)),
            category = parts(4),
            location = if (parts(5).nonEmpty) Some(parts(5)) else None,
            notes = if (parts.length > 6 && parts(6).nonEmpty) Some(parts(6)) else None
          )
        }
      }
    }
  }
}
