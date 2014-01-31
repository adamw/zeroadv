package zeroadv.position

import zeroadv._

class ReceivedAdvParser {
  private val gimbalPrefix_1_to_14 = hexStringToByteArray("11 07 AD 77 00 C6 A0 00 99 B2 E2 11 4C 24").toList
  private val gimbalInfix_16_to_22 = hexStringToByteArray("4A 0C 96 0C FF 8C 00").toList

  private val ibeaconPrefix_1_to_9 = hexStringToByteArray("02 01 1A 1A FF 4C 00 02 15").toList

  def parse(adv: ReceivedAdv): Option[BeaconSpotting] = {
    val beacon = if (isGimbalAdv(adv)) {
      Some(parseGimbal(adv))
    } else if (isIBeaconAdv(adv)) {
      Some(parseIBeacon(adv))
    } else {
      None
    }

    beacon.map(b => BeaconSpotting(b, adv.agent, TimedRssi(adv.when, adv.rssi)))
  }

  private def isGimbalAdv(adv: ReceivedAdv) = adv.adv.size == 31 &&
      adv.adv.slice(0, 14).toList == gimbalPrefix_1_to_14 &&
      adv.adv.slice(15, 22).toList == gimbalInfix_16_to_22

  private def parseGimbal(adv: ReceivedAdv) = {
    GimbalBeacon(toUnsigned(adv.adv(14)), byteArrayToHexString(adv.adv.slice(22, 31)))
  }

  private def isIBeaconAdv(adv: ReceivedAdv) = adv.adv.size == 30 &&
      adv.adv.slice(0, 9).toList == ibeaconPrefix_1_to_9

  private def parseIBeacon(adv: ReceivedAdv) = {
    IBeaconBeacon(
      byteArrayToHexString(adv.adv.slice(9, 25)),
      toUnsigned(adv.adv(25))*256 + toUnsigned(adv.adv(26)),
      toUnsigned(adv.adv(27))*256 + toUnsigned(adv.adv(28)),
      toUnsigned(adv.adv(29)) - 256)
  }
}
