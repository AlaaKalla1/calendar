import org.scalatest.funsuite.AnyFunSuite

class ReminderUtilsTest extends AnyFunSuite:

  test("ReminderUtils should initialize the timer correctly") {
    val reminderUtils = new ReminderUtils()
    assert(reminderUtils.reminderTimer != null)
  }

  test("ReminderUtils.playReminderSound should be callable without crash") {
    val reminderUtils = new ReminderUtils()
    try
      reminderUtils.playReminderSound()
      succeed  
    catch
      case e: Exception => fail("playReminderSound() threw an exception")
  }
