package zeroadv.position

import zeroadv.{BeaconSpotting, PositionedBeacon, BeaconSpottings, PositionedAgents}

class BeaconPosFromSpottings(beaconDistance: BeaconDistance) {

  def calculate(
    agents: PositionedAgents,
    spottings: BeaconSpottings): PositionedBeacon = {

    val distances = spottings.history.map { case (agent, timedRssi) =>
      val dist = beaconDistance.distanceToBeacon(BeaconSpotting(spottings.beacon, agent, timedRssi.head))

      s"$agent -> ${dist.prettyString}m (${timedRssi.head.rssi} dBm)"
    }.mkString("; ")

    println(distances)

    null
  }
}
