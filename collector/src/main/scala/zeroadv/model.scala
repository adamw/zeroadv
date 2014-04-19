package zeroadv

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

sealed trait PositioningEvent {
  def when: DateTime
}

case class ReceivedAdv(when: DateTime, agent: Agent, adv: Array[Byte], rssi: Int) extends PositioningEvent {
  def advStr = byteArrayToHexString(adv)

  def whenStr =  DateTimeFormat.mediumTime().print(when)

  override def toString = {
    s"$agent@[$whenStr]: $advStr, RSSI = $rssi"
  }
}

case class TimedRssi(when: DateTime, rssi: Int)

sealed trait Beacon
case class GimbalBeacon(temp: Int, id: String) extends Beacon
case class IBeaconBeacon(uuid: String, major: Int, minor: Int, txPower: Int) extends Beacon

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

case class DimM(coord: Double) extends AnyVal {
  def prettyString = "%02.2f".format(coord)
  def +(o: DimM) = DimM(coord + o.coord)
  def -(o: DimM) = DimM(coord - o.coord)
  def *(o: DimM) = DimM(coord * o.coord)
  def sqrt = DimM(math.sqrt(coord))

  override def toString = coord + "m"
}

case class PosM(x: DimM, y: DimM) {
  override def toString = s"($x, $y)"

  def -(o: PosM) = PosM(x - o.x, y - o.y)
}

object PosM {
  def dist(p1: PosM, p2: PosM): DimM = {
    val xd = p1.x - p2.x
    val yd = p1.y - p2.y
    (xd*xd + yd*yd).sqrt
  }
}

case class PositionedAgent(agent: Agent, pos: PosM)

case class PositionedAgents(agents: List[PositionedAgent])

case class PositionedBeacon(beacon: Beacon, pos: PosM)

case class MarkPosition(when: DateTime, pos: PosM) extends PositioningEvent

case class EndMark(when: DateTime) extends PositioningEvent