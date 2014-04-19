package zeroadv.position

import com.typesafe.scalalogging.slf4j.Logging
import zeroadv._
import zeroadv.PositionedAgents
import zeroadv.BeaconSpotting
import zeroadv.BeaconSpottings
import zeroadv.PositionedBeacon

class DistanceBasedBeaconPos(
  beaconDistance: BeaconDistance,
  calculatePosition: CalculatePosition) extends BeaconPosFromSpottings with Logging {

  def calculate(
    agents: PositionedAgents,
    spottings: BeaconSpottings): PositionedBeacon = {

    val points = for {
      (agent, timedRssi) <- spottings.history
      positionedAgent <- agents.agents.find(_.agent == agent)
    } yield {
      val rssis = timedRssi.map(_.rssi).sorted
      val rssi = if (rssis.size > 2) {
        rssis.drop(1).dropRight(1).sum / (rssis.size - 2)
      } else {
        rssis.head
      }

      val dist = beaconDistance.distanceToBeacon(BeaconSpotting(spottings.beacon, agent, TimedRssi(null, rssi)))
      (positionedAgent.pos, dist)
    }

    val beaconPos = calculatePosition.calculate(points.toList)._1

    PositionedBeacon(spottings.beacon, beaconPos)
  }
}
