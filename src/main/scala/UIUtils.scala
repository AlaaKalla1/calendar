import scalafx.scene.control.Label
import scala.collection.mutable
import java.time.LocalDate


class UIUtils:

  def highlightRange(hourCells: mutable.Map[Int, Label], calendar: Calendar, currentDay: LocalDate, from: Int, to: Int): Unit =
    val start = math.min(from, to)
    val end = math.max(from, to)

    for (i <- 0 to 23) do
      hourCells.get(i).foreach { label =>
        val hasEvent = calendar.listEvents(currentDay).exists { e =>
          val hStart = e.startTime.getHour
          val hEnd = e.endTime.getHour
          i >= hStart && i <= hEnd
        }

        updateCellStyle(label, hasEvent, start, end, i)

      }



  private def updateCellStyle(label: Label, hasEvent: Boolean, start: Int, end: Int, hour: Int): Unit =
    if !hasEvent then
      if hour >= start && hour <= end && start >= 0 then
        label.style = "-fx-background-color: #cce5ff; -fx-border-color: #0000ff;"
      else
        label.style = "-fx-background-color: #ffffff; -fx-border-color: #dddddd;"


  def getColorForCategory(category: String): String =
    category.toLowerCase match
      case "urgent"   => "#FF0000"
      case "study"    => "#FFFF00"
      case "meeting"  => "#FFA500"
      case "personal" => "#000000"
      case "birthday" => "#800080"
      case "exercise" => "#008000"
      case "work"     => "#0000FF"
      case _          => "#b38add"

