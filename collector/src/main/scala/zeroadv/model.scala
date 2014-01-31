package zeroadv

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

case class ReceivedAdv(when: DateTime, agent: Agent, adv: Array[Byte], rssi: Int) {
  def advStr = byteArrayToHexString(adv)

  def whenStr =  DateTimeFormat.mediumTime().print(when)

  override def toString = {
    s"$agent@[$whenStr]: $advStr, RSSI = $rssi"
  }
}

case class TimedRssi(when: DateTime, rssi: Int)

trait Beacon
case class GimbalBeacon(temp: Int, id: String) extends Beacon
case class EstimoteBeacon() extends Beacon

case class BeaconSpotting(beacon: Beacon, agent: Agent, rssi: TimedRssi)

case class BeaconSpottings(beacon: Beacon, history: Map[Agent, List[TimedRssi]]) {
  def addSpotting(spotting: BeaconSpotting, rssisLimit: Int): BeaconSpottings = {
    val newRssis = (spotting.rssi :: history.getOrElse(spotting.agent, Nil)).take(rssisLimit)
    copy(history = history + (spotting.agent -> newRssis))
  }
}

case class BeaconsSpottings(spottings: Map[Beacon, BeaconSpottings]) {
  def forBeacon(b: Beacon) = spottings.getOrElse(b, BeaconSpottings(b, Map()))

  def addSpotting(spotting: BeaconSpotting, rssisLimit: Int): (BeaconSpottings, BeaconsSpottings) = {
    val b = spotting.beacon
    val currentBS = forBeacon(b)
    val newBS = currentBS.addSpotting(spotting, rssisLimit)
    (newBS, copy(spottings = spottings + (b -> newBS)))
  }
}

case class Agent(name: String) {
  override def toString = name
}

case class CoordM(coord: Double) extends AnyVal

case class PosM(x: CoordM, y: CoordM)

case class PositionedAgent(agent: Agent, pos: PosM)

case class PositionedAgents(agents: List[PositionedAgent])

case class PositionedBeacon(beacon: Beacon, pos: PosM)