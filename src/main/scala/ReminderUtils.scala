import java.io.File
import java.time.{LocalDate, LocalTime, Duration as JDuration}
import java.util.{Properties, Timer, TimerTask}
import javax.mail.*
import javax.mail.internet.*
import javax.sound.sampled.*

class ReminderUtils:

  val reminderTimer = new Timer(true)

  def playReminderSound(): Unit =
    try
      val audioInput = AudioSystem.getAudioInputStream(new File("ding.wav"))
      val clip = AudioSystem.getClip()
      clip.open(audioInput)
      clip.start()
    catch case e: Exception => println("Audio error: " + e.getMessage)

  def sendEmailReminder(event: Event): Unit =
    val to = "alaa.kalla@aalto.fi"
    val from = "skam6809@gmail.com"
    val password = "knfm dcwl gzpd fjbw"

    val props = new Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.port", "587")

    val session = Session.getInstance(props, new Authenticator() {
      override protected def getPasswordAuthentication: PasswordAuthentication =
        new PasswordAuthentication(from, password)
    })

    try
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(from))
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to).asInstanceOf[Array[Address]])
      message.setSubject("Reminder: " + event.name)
      message.setText(s"Don't forget your event: ${event.name} on ${event.date} at ${event.startTime}")
      Transport.send(message)
    catch case e: Exception => println("Email error: " + e.getMessage)

  def scheduleReminder(event: Event): Unit =
    val reminderTime = event.startTime.minusMinutes(60)
    val now = LocalTime.now()
    if event.date.isEqual(LocalDate.now()) && reminderTime.isAfter(now) then
      val delayMillis = JDuration.between(now, reminderTime).toMillis
      reminderTimer.schedule(new TimerTask {
        def run(): Unit =
          playReminderSound()
          sendEmailReminder(event)
      }, delayMillis)
