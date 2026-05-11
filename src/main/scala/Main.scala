import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.animation.FadeTransition
import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.*
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.util.Duration
import java.time.{LocalDate, LocalDateTime, LocalTime, YearMonth}
import scala.collection.mutable
import java.io.{BufferedWriter, FileWriter}


var dragStartHour: Option[Int] = None
var dragEndHour: Option[Int] = None
val hourCells = mutable.Map[Int, Label]()




object CalendarApp extends JFXApp3:


  val holidayUtils = new HolidayUtils()
  val calendar = new Calendar()
  val reminderUtils = new ReminderUtils()
  val uiUtils = new UIUtils()


  var currentMonth: YearMonth = YearMonth.now()
  var currentYear: Int = currentMonth.getYear
  var currentDay: LocalDate = LocalDate.now()
  var currentWeekStart: LocalDate = currentDay.minusDays(currentDay.getDayOfWeek.getValue % 7)


  def start(): Unit =
    calendar.loadFromFile()


    val calendarGrid = new GridPane()
    val monthLabel = new Label()
    val eventsBox = new VBox()
    val upcomingBox = new VBox()
    val rootPane = new BorderPane()
    val prevBtn = new Button("<")
    val nextBtn = new Button(">")
    val addEventBtn = new Button("+ Add Event")
    val listEventBtn = new Button("List Events")
    val darkModeToggle = new Button("Night mood")



    calendarGrid.hgap = 9
    calendarGrid.vgap = 9
    calendarGrid.padding = Insets(10)
    calendarGrid.alignment = Pos.Center
    val yearSelector = new ChoiceBox[Int]() {
      items.value.addAll((2000 to 2030)*)
      value = currentYear
    }

    val viewMode = new ChoiceBox[String]() {
      items.value.addAll("Year", "Month", "Week", "Day")
      value = "Month"
    }

    def saveEventsToICS(filename: String): Unit =
      val bw = new BufferedWriter(new FileWriter(filename))
      try
        bw.write("BEGIN:VCALENDAR\n")
        bw.write("VERSION:2.0\n")
        bw.write("PRODID:-//yourcompany//calendarapp//EN\n")
        for event <- calendar.allEventsSorted do
          bw.write("BEGIN:VEVENT\n")
          bw.write(s"UID:${event.hashCode}@calendarapp\n")
          bw.write(s"DTSTAMP:${LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))}\n")
          bw.write(s"DTSTART:${event.date.toString.replace("-", "")}T${event.startTime.toString.replace(":", "")}00Z\n")
          bw.write(s"DTEND:${event.date.toString.replace("-", "")}T${event.endTime.toString.replace(":", "")}00Z\n")
          bw.write(s"SUMMARY:${event.name}\n")
          event.notes.foreach(notes => bw.write(s"DESCRIPTION:$notes\n"))
          event.location.foreach(location => bw.write(s"LOCATION:$location\n"))
          bw.write("END:VEVENT\n")
        bw.write("END:VCALENDAR\n")
      finally
        bw.close()



    val modalBox = new VBox(10)
    modalBox.style = "-fx-background-color: white; -fx-padding: 20; -fx-border-radius: 10; -fx-background-radius: 10;"
    modalBox.effect = new DropShadow()
    modalBox.alignment = Pos.Center
    modalBox.maxWidth = 300
    modalBox.visible = false

    val nameField = new TextField() {
      promptText = "Event Name"
    }
    val startDateTimeField = new TextField() {
      promptText = "Start (YYYY-MM-DD HH:MM)"
    }
    val endDateTimeField = new TextField() {
      promptText = "End (YYYY-MM-DD HH:MM)"
    }

    val categoryField = new TextField() {
      promptText = "Category"
    }
    val searchField = new TextField() { promptText = "Search..." }
    val searchBtn = new Button("Search")

    val descriptionField = new TextField() {
      promptText = "Description"
    }
    val exportBtn = new Button("Export ICS")

    exportBtn.onAction = _ =>
      saveEventsToICS("mycalendar.ics")

    val topBar = new HBox(10, prevBtn, monthLabel, nextBtn, viewMode, yearSelector, addEventBtn, listEventBtn, darkModeToggle, searchField, searchBtn, exportBtn)
    topBar.alignment = Pos.Center
    topBar.padding = Insets(10)
    val calendarView = new VBox(10, topBar, calendarGrid, new Label("Upcoming Events"), upcomingBox, eventsBox)
    calendarView.alignment = Pos.Center
    calendarView.vgrow = scalafx.scene.layout.Priority.Always
    calendarView.padding = Insets(20)

    rootPane.center = calendarView
    BorderPane.setAlignment(calendarView, Pos.Center)



    val categoryColorLabel = new Label("← Color Preview")
    categoryColorLabel.style = "-fx-background-color: #b38add; -fx-text-fill: white; -fx-padding: 5;"

    categoryField.text.onChange { (_, _, newValue) =>
      val color = uiUtils.getColorForCategory(newValue)

      categoryColorLabel.style = s"-fx-background-color: $color; -fx-text-fill: white; -fx-padding: 5;"
    }

    val submitBtn = new Button("Add")
    val cancelBtn = new Button("Cancel")
    val modalControls = new HBox(10, submitBtn, cancelBtn)
    modalControls.alignment = Pos.Center

    modalBox.children ++= Seq(
      nameField,
      startDateTimeField,
      endDateTimeField,
      categoryField,
      descriptionField,
      categoryColorLabel,
      modalControls
    )



    val overlay = new Rectangle()
    overlay.fill = Color.rgb(0, 0, 0, 0.5)
    overlay.visible = false

    val stack = new StackPane()
    stack.children ++= Seq(rootPane, overlay, modalBox)
    StackPane.setAlignment(modalBox, Pos.Center)

    stage = new JFXApp3.PrimaryStage:
      title = "Calendar"
      scene = new Scene(stack, 1000, 700)
    overlay.width <== stage.width
    overlay.height <== stage.height

    val centerContent = new VBox(20, topBar, calendarGrid, new Label("Upcoming Events"), upcomingBox, new Label("Selected Date Events"), eventsBox)
    centerContent.alignment = Pos.Center
    centerContent.padding = Insets(20)

    rootPane.center = centerContent


    def fadeIn(targetNode: scalafx.scene.Node): Unit =
      val fade = new FadeTransition {
        duration = Duration(500)
        fromValue = 0.0
        toValue = 1.0
        node = targetNode
      }
      fade.play()

    def toggleDarkMode(): Unit =
      val isDark = rootPane.style.value.contains("background-color: #121212")
      if isDark then
        rootPane.style = ""
        calendarGrid.style = ""
      else
        rootPane.style = "-fx-background-color: #121212; -fx-text-fill: white;"
        calendarGrid.style = "-fx-background-color: #1e1e1e;"

    darkModeToggle.onAction = _ => toggleDarkMode()

      overlay.fill = Color.rgb(0, 0, 0, 0.5)

    def showEventsForDate(date: LocalDate): Unit =
      val events = calendar.listEvents(date)
      eventsBox.children.clear()
      eventsBox.children += new Label(s"Events for $date")
      if events.isEmpty then
        eventsBox.children += new Label("No Events")
      else
        events.foreach { ev =>


          val descriptionText = ev.notes.map(n => s"\nDesc: $n").getOrElse("")
          val label = new Label(s"${ev.name} (${ev.startTime}-${ev.endTime}) [${ev.category}]$descriptionText")
          label.setStyle(s"-fx-cursor: hand; -fx-text-fill: ${uiUtils.getColorForCategory(ev.category)};")


          label.onMouseClicked = { e =>
            val contextMenu = new javafx.scene.control.ContextMenu()

            val editItem = new javafx.scene.control.MenuItem("Edit")
            editItem.setOnAction(_ => {
              val dialog = new javafx.scene.control.TextInputDialog(ev.name)
              dialog.setTitle("Edit Event")
              dialog.setHeaderText("Edit event title")
              dialog.setContentText("New title:")
              val result = dialog.showAndWait()
              if result.isPresent then
                calendar.removeEvent(ev.name, ev.date)
                calendar.addEvent(ev.copy(name = result.get()))
                updateCalendar()
            })

            val deleteItem = new javafx.scene.control.MenuItem("Delete")
            deleteItem.setOnAction(_ => {
              val alert = new Alert(AlertType.Confirmation) {
                title = "Delete Event"
                headerText = s"Delete '${ev.name}'?"
                contentText = "Are you sure you want to delete this event?"
              }
              val result = alert.showAndWait()
              if result.isDefined && result.get == scalafx.scene.control.ButtonType.OK then
                calendar.removeEvent(ev.name, ev.date)
                updateCalendar()
            })

            contextMenu.getItems.addAll(editItem, deleteItem)
            contextMenu.show(label, e.screenX, e.screenY)
          }

          eventsBox.children += label
        }


    viewMode.onAction = _ => updateCalendar()
    
    yearSelector.onAction = _ => {
      currentYear = yearSelector.value.value;
      updateCalendar()
    }

    prevBtn.onAction = _ =>
      viewMode.value.value match
        case "Month" => currentMonth = currentMonth.minusMonths(1)
        case "Year" => currentYear -= 1
        case "Week" => currentWeekStart = currentWeekStart.minusDays(7)
        case "Day" => currentDay = currentDay.minusDays(1)
      updateCalendar()

    nextBtn.onAction = _ =>
      viewMode.value.value match
        case "Month" => currentMonth = currentMonth.plusMonths(1)
        case "Year" => currentYear += 1
        case "Week" => currentWeekStart = currentWeekStart.plusDays(7)
        case "Day" => currentDay = currentDay.plusDays(1)


    updateCalendar()


    listEventBtn.onAction = _ =>
      val dialog = new javafx.scene.control.TextInputDialog()
      dialog.setTitle("List Events")
      dialog.setHeaderText("Enter date to view events")
      dialog.setContentText("Date (YYYY-MM-DD):")
      val result = dialog.showAndWait()
      if result.isPresent then
        try showEventsForDate(LocalDate.parse(result.get()))
        catch case e: Exception => new Alert(AlertType.Error) {
          title = "Invalid Date"
          contentText = e.getMessage
        }.showAndWait()


    def updateDragHighlight(): Unit =
      (for
        start <- dragStartHour
        end <- dragEndHour
      yield {
        val from = math.min(start, end)
        val to = math.max(start, end)
        for i <- 0 to 23 do
          if i >= from && i <= to then
            hourCells(i).style = "-fx-background-color: #cce5ff; -fx-border-color: #0000ff;"
          else
            hourCells(i).style = "-fx-background-color: #ffffff; -fx-border-color: #dddddd;"
      })

    def clearDragHighlight(): Unit =
      for i <- 0 to 23 do
        hourCells(i).style = "-fx-background-color: #ffffff; -fx-border-color: #dddddd;"


    def updateCalendar(): Unit =
      calendarGrid.children.clear()
      calendarGrid.columnConstraints.clear()
      upcomingBox.children.clear()
      eventsBox.children.clear()
      val holidays = holidayUtils.finnishHolidays(currentYear)//////////////////////////////////////////

      val upcoming = calendar.allEventsSorted.filter(_.date.isAfter(LocalDate.now())).take(10)
      for ev <- upcoming do
        val descriptionText = ev.notes.map(n => s"\nDesc: $n").getOrElse("")
        val label = new Label(
          s"${ev.name} (${ev.date} ${ev.startTime}-${ev.endTime}) [${ev.category}]$descriptionText"
        )

        label.setStyle(s"-fx-text-fill: ${uiUtils.getColorForCategory(ev.category)};")

        upcomingBox.children += label

      val mode = viewMode.value.value

      if mode == "Year" then
        monthLabel.text = s"Year $currentYear"
        for i <- 0 until 12 do
          val ym = YearMonth.of(currentYear, i + 1)
          val btn = new Button(ym.getMonth.toString.capitalize)
          btn.prefWidth = 120
          btn.onAction = _ =>
            currentMonth = ym
            viewMode.value = "Month"
            updateCalendar()
          calendarGrid.add(btn, i % 4, i / 4)

      else if mode == "Month" then
        val firstOfMonth = currentMonth.atDay(1)
        val dayOfWeekIndex = firstOfMonth.getDayOfWeek.getValue % 7
        val lengthOfMonth = currentMonth.lengthOfMonth()
        monthLabel.text = s"${currentMonth.getMonth} ${currentMonth.getYear}"
        var row = 1
        var col = dayOfWeekIndex

        for day <- 1 to lengthOfMonth do
          val date = currentMonth.atDay(day)
          val btn = new Button(day.toString)
          btn.prefWidth = 60
          btn.prefHeight = 60
          if date.isEqual(LocalDate.now()) then
            btn.setStyle("-fx-background-color: #b38add; -fx-text-fill: white;")


          if holidays.contains(date) then
            btn.setStyle("-fx-background-color: #FF0000; -fx-text-fill: black;")
            btn.tooltip = new Tooltip(holidays(date))




          btn.onAction = _ => showEventsForDate(date)

          calendarGrid.add(btn, col, row)
          col += 1
          if col > 6 then
            col = 0;
            row += 1

      else if mode == "Week" then
        monthLabel.text = s"Week of $currentWeekStart"
        for i <- 0 to 6 do
          val date = currentWeekStart.plusDays(i)
          val btn = new Button(s"${date.getDayOfWeek.toString.take(6)}\n${date.getDayOfMonth}")
          btn.wrapText = true

          if holidays.contains(date) then
            btn.setStyle("-fx-background-color: #FF0000; -fx-text-fill: white;")
            btn.tooltip = new Tooltip(holidays(date))

          btn.onAction = _ => showEventsForDate(date)
          calendarGrid.add(btn, i, 1)

      else if mode == "Day" then
        monthLabel.text = s"${currentDay.getDayOfWeek} ${currentDay}"

        val col1 = new scalafx.scene.layout.ColumnConstraints()
        col1.prefWidth = 60
        val col2 = new scalafx.scene.layout.ColumnConstraints()
        col2.hgrow = Priority.Always
        calendarGrid.columnConstraints.addAll(col1, col2)

        for hour <- 0 to 23 do
          val label = new Label(f"$hour%02d:00")
          label.prefWidth = 60
          label.style = "-fx-background-color: #f0f0f0;"

          val cell = new Label()
          cell.prefHeight = 40
          cell.prefWidth = 800
          cell.style = "-fx-background-color: #ffffff; -fx-border-color: #dddddd;"

          hourCells(hour) = cell

          val events = calendar.listEvents(currentDay).filter(e =>
            !e.startTime.isAfter(LocalTime.of(hour, 0)) &&
              e.endTime.isAfter(LocalTime.of(hour, 0))
          )

          if events.nonEmpty then
            val ev = events.head
            cell.text = s"${ev.name} (${ev.startTime}-${ev.endTime})"
            cell.style = s"-fx-background-color: ${uiUtils.getColorForCategory(ev.category)}; -fx-text-fill: white; -fx-padding: 5px;"



          cell.onMousePressed = _ =>
            dragStartHour = Some(hour)
            dragEndHour = Some(hour)
            for (i <- hourCells.keys) do
              if hourCells(i).style().contains("#cce5ff") then
                hourCells(i).style = "-fx-background-color: #ffffff; -fx-border-color: #dddddd;"
            cell.style = "-fx-background-color: #cce5ff; -fx-border-color: #0000ff;"

          cell.onMouseDragged = event =>
            val relativeY = event.sceneY - calendarGrid.layoutY.value
            val draggedHour = (relativeY / 40).toInt
            val safeDraggedHour = math.max(0, math.min(23, draggedHour))
            dragEndHour = Some(safeDraggedHour)

            (for start <- dragStartHour; end <- dragEndHour yield {
              val from = math.min(start, end)
              val to = math.max(start, end)
              for i <- 0 to 23 do
                if i >= from && i <= to then
                  hourCells(i).style = "-fx-background-color: #cce5ff; -fx-border-color: #0000ff;"
                else
                  hourCells(i).style = "-fx-background-color: #ffffff; -fx-border-color: #dddddd;"
            })

          cell.onMouseReleased = _ =>
            (for
              start <- dragStartHour
              end <- dragEndHour
            yield {
              val from = math.min(start, end)
              val to = math.max(start, end)

              val startDateTime = LocalDateTime.of(currentDay, LocalTime.of(from, 0))
              val endDateTime = LocalDateTime.of(currentDay, LocalTime.of(to + 1, 0))

              nameField.text = s"Event $from-$to"
              startDateTimeField.text = startDateTime.toString.replace('T', ' ')
              endDateTimeField.text = endDateTime.toString.replace('T', ' ')

              overlay.visible = true
              modalBox.visible = true
              fadeIn(modalBox)
            }).foreach(_ => {
              dragStartHour = None
              dragEndHour = None
              clearDragHighlight()
            })

          calendarGrid.addRow(hour, label, cell)

      fadeIn(calendarGrid)

    addEventBtn.onAction = _ => {
      overlay.visible = true
      modalBox.visible = true
      fadeIn(modalBox)
    }

    viewMode.value.onChange { (_, _, _) =>
      updateCalendar()
    }

    cancelBtn.onAction = _ =>
      overlay.visible = false
      modalBox.visible = false
      nameField.clear()
      startDateTimeField.clear()
      endDateTimeField.clear()
      categoryField.clear()
      descriptionField.clear()


    submitBtn.onAction = _ => {
      try {
        val startDateTime = LocalDateTime.parse(startDateTimeField.text.value.replace(" ", "T"))
        val endDateTime = LocalDateTime.parse(endDateTimeField.text.value.replace(" ", "T"))

        val event = Event(
          name = nameField.text.value,
          date = startDateTime.toLocalDate,
          startTime = startDateTime.toLocalTime,
          endTime = endDateTime.toLocalTime,
          category = categoryField.text.value,
          location = None,
          notes = Some(descriptionField.text.value),
          reminderMinutesBefore = None
        )

        calendar.addEvent(event)
        reminderUtils.scheduleReminder(event)
        reminderUtils.playReminderSound()


        updateCalendar()
        cancelBtn.fire()
      } catch {
        case e: Exception => new Alert(AlertType.Error) {
          title = "Invalid Input"
          contentText = e.getMessage
        }.showAndWait()
      }
    }


    searchBtn.onAction = _ =>
      val query = searchField.text.value.toLowerCase
      val matchingEvents = calendar.allEventsSorted.filter { ev =>
        ev.name.toLowerCase.contains(query) ||
          ev.notes.exists(_.toLowerCase.contains(query)) ||
          ev.category.toLowerCase.contains(query)
      }



      eventsBox.children.clear()
      if matchingEvents.isEmpty then
        eventsBox.children += new Label("No matching events.")
      else
        matchingEvents.foreach { ev =>
          val label = new Label(
            s"${ev.name} (${ev.date} ${ev.startTime}-${ev.endTime}) [${ev.category}]\nDesc: ${ev.notes.getOrElse("")}"
          )
          label.setStyle(s"-fx-text-fill: ${uiUtils.getColorForCategory(ev.category)};")
          eventsBox.children += label
        }
        
        
        
    viewMode.onAction = _ => updateCalendar()
    
    yearSelector.onAction = _ => {
      currentYear = yearSelector.value.value;
      updateCalendar()
    }

    prevBtn.onAction = _ =>
      viewMode.value.value match
        case "Month" => currentMonth = currentMonth.minusMonths(1)
        case "Year" => currentYear -= 1
        case "Week" => currentWeekStart = currentWeekStart.minusDays(7)
        case "Day" => currentDay = currentDay.minusDays(1)
      updateCalendar()

    nextBtn.onAction = _ =>
      viewMode.value.value match
        case "Month" => currentMonth = currentMonth.plusMonths(1)
        case "Year" => currentYear += 1
        case "Week" => currentWeekStart = currentWeekStart.plusDays(7)
        case "Day" => currentDay = currentDay.plusDays(1)
      updateCalendar()


    listEventBtn.onAction = _ =>
      val dialog = new javafx.scene.control.TextInputDialog()
      dialog.setTitle("List Events")
      dialog.setHeaderText("Enter date to view events")
      dialog.setContentText("Date (YYYY-MM-DD):")
      val result = dialog.showAndWait()
      if result.isPresent then
        try showEventsForDate(LocalDate.parse(result.get()))
        catch case e: Exception => new Alert(AlertType.Error) {
          title = "Invalid Date"
          contentText = e.getMessage
        }.showAndWait()




    updateCalendar()