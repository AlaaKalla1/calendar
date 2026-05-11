import java.time.{LocalDate, Month}

class HolidayUtils:

  def finnishHolidays(year: Int): Map[LocalDate, String] =
    Map(
      LocalDate.of(year, 1, 1)  -> "Uudenvuodenpäivä ",
      LocalDate.of(year, 1, 6)  -> "Loppiainen ",
      easterSunday(year).minusDays(2) -> "Pitkäperjantai ",
      easterSunday(year) -> "Pääsiäispäivä ",
      easterSunday(year).plusDays(1) -> "Toinen pääsiäispäivä ",
      LocalDate.of(year, 4, 30) -> "Vappuaatto ",
      LocalDate.of(year, 5, 1)  -> "Vappupäivä ",
      LocalDate.of(year, 5, 29) -> "Helatorstai ",
      easterSunday(year).plusDays(49) -> "Helatorstai ",
      easterSunday(year).plusDays(49 + 10) -> "Helluntai ",
      midsummerEve(year) -> "Juhannusaatto ",
      midsummerDay(year) -> "Juhannuspäivä ",
      allSaintsDay(year) -> "Pyhäinpäivä ",
      LocalDate.of(year, 12, 6) -> "Itsenäisyyspäivä ",
      LocalDate.of(year, 12, 24) -> "Jouluaatto ",
      LocalDate.of(year, 12, 25) -> "Joulupäivä ",
      LocalDate.of(year, 12, 26) -> "Tapaninpäivä ",
      LocalDate.of(year, 12, 31) -> "Uudenvuodenaatto "
    )

  private def easterSunday(year: Int): LocalDate =

    val a = year % 19
    val b = year / 100
    val c = year % 100
    val d = b / 4
    val e = b % 4
    val f = (b + 8) / 25
    val g = (b - f + 1) / 3
    val h = (19 * a + b - d - g + 15) % 30
    val i = c / 4
    val k = c % 4
    val l = (32 + 2 * e + 2 * i - h - k) % 7
    val m = (a + 11 * h + 22 * l) / 451
    val month = (h + l - 7 * m + 114) / 31
    val day = ((h + l - 7 * m + 114) % 31) + 1
    LocalDate.of(year, month, day)

  private def midsummerEve(year: Int): LocalDate =
    val midsummer = LocalDate.of(year, Month.JUNE, 20)
    (0 until 7)
      .map(midsummer.plusDays(_))
      .find(_.getDayOfWeek.getValue == 5)
      .get

  private def midsummerDay(year: Int): LocalDate =
    midsummerEve(year).plusDays(1)

  private def allSaintsDay(year: Int): LocalDate =
    val firstNovember = LocalDate.of(year, Month.NOVEMBER, 1)
    (0 until 6)
      .map(firstNovember.plusDays(_))
      .find(d => d.getDayOfWeek.getValue == 6 || d.getDayOfWeek.getValue == 7)
      .get
