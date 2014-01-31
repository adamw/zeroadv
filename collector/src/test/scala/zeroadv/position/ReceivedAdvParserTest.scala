package zeroadv.position

import org.scalatest.{FlatSpec, ShouldMatchers}
import org.joda.time.DateTime
import zeroadv._

class ReceivedAdvParserTest extends FlatSpec with ShouldMatchers {
  val agent = Agent("x")
  val when = new DateTime(2014, 9, 20, 10, 4)
  val rssi = -80

  def createAdv(advString: String) = {
    ReceivedAdv(when, agent, hexStringToByteArray(advString), -80)
  }

  val testDatas = List(
    ("11 07 AD 77 00 C6 A0 00 99 B2 E2 11 4C 24 88 4A 0C 96 0C FF 8C 00 2B AE A6 18 F3 18 D1 22 B8",
      GimbalBeacon(136, "2B AE A6 18 F3 18 D1 22 B8")),
    ("11 07 AD 77 00 C6 A0 00 99 B2 E2 11 4C 24 54 4A 0C 96 0C FF 8C 00 90 B5 88 A8 57 8A 18 EA 63",
      GimbalBeacon(84, "90 B5 88 A8 57 8A 18 EA 63")),
    ("11 07 AD 77 00 C6 A0 00 99 B2 E2 11 4C 24 86 4A 0C 96 0C FF 8C 00 EF 82 55 26 B0 AA 96 DF A4",
      GimbalBeacon(134, "EF 82 55 26 B0 AA 96 DF A4")),
    ("02 01 1A 1A FF 4C 00 02 15 B9 40 7F 30 F5 F8 46 6E AF F9 25 55 6B 57 FE 6D 00 49 00 0A C5",
      IBeaconBeacon("B9 40 7F 30 F5 F8 46 6E AF F9 25 55 6B 57 FE 6D", 73, 10, -59))
  )

  testDatas.foreach { testData =>
    it should s"parse ${testData._1}" in {
      val expectedSpotting = BeaconSpotting(testData._2, agent, TimedRssi(when, rssi))
      new ReceivedAdvParser().parse(createAdv(testData._1)) should be (Some(expectedSpotting))
    }
  }

  it should "not parse an unknown sequence" in {
    val adv = createAdv("11 07 AD FF 8C 00 90 B5 88 A8 57 8A 18 EA 63 AB 3E 2B 02 01 03 01 0E 06 C0 50 7C 21 1F 11 07")
    new ReceivedAdvParser().parse(adv) should be (None)
  }
}
