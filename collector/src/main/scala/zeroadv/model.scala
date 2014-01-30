package zeroadv

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

case class ReceivedAdv(when: DateTime, agent: Agent, adv: Array[Byte], rssi: Int) {
  override def toString = {
    val advStr = adv.map(toUnsigned).map(Integer.toHexString)
      .map(_.toUpperCase).map(x => if (x.length < 2) "0" + x else x)
      .toList.mkString(" ")

    val whenStr = DateTimeFormat.mediumTime().print(when)

    s"$agent@[$whenStr]: $advStr, RSSI = $rssi"
  }
}

case class TimedRssi(when: DateTime, rssi: Int)

trait Beacon
case class GimbalBeacon() extends Beacon
case class EstimoteBeacon() extends Beacon

case class BeaconSpotting(beacon: Beacon, agent: Agent, rssi: TimedRssi)

case class BeaconSpottings(beacon: Beacon, history: Map[Agent, List[TimedRssi]]) {
  def addSpotting(spotting: BeaconSpotting, paramsLimit: Int): BeaconSpottings = {
    val newRssis = (spotting.rssi :: history.getOrElse(spotting.agent, Nil)).take(paramsLimit)
    copy(history = history + (spotting.agent -> newRssis))
  }
}

case class BeaconsSpottings(spottings: Map[Beacon, BeaconSpottings]) {
  def addSpotting(spotting: BeaconSpotting, paramsLimit: Int): BeaconsSpottings = {
    val b = spotting.beacon
    val currentBS = spottings.getOrElse(b, BeaconSpottings(b, Map()))
    val newBS = currentBS.addSpotting(spotting, paramsLimit)
    copy(spottings = spottings + (b -> newBS))
  }
}

case class Agent(name: String) {
  override def toString = name
}

case class CoordM(coord: Double) extends AnyVal

case class PosM(x: CoordM, y: CoordM)

case class PositionedAgent(agent: Agent, pos: PosM)

case class PositionedBeacon(beacon: Beacon, pos: PosM)