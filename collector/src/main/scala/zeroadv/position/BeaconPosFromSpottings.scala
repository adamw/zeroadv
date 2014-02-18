package zeroadv.position

import zeroadv.{BeaconSpotting, PositionedBeacon, BeaconSpottings, PositionedAgents}
import com.typesafe.scalalogging.slf4j.Logging

class BeaconPosFromSpottings(
  beaconDistance: BeaconDistance,
  calculatePosition: CalculatePosition) extends Logging {

  def calculate(
    agents: PositionedAgents,
    spottings: BeaconSpottings): PositionedBeacon = {

    val points = for {
      (agent, timedRssi) <- spottings.history
      positionedAgent <- agents.agents.find(_.agent == agent)
    } yield {
      val dist = beaconDistance.distanceToBeacon(BeaconSpotting(spottings.beacon, agent, timedRssi.head))
      (positionedAgent.pos, dist)
    }

    val beaconPos = calculatePosition.calculate(points.toList)._1

    PositionedBeacon(spottings.beacon, beaconPos)
  }
}
