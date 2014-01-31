package zeroadv

class BeaconPosFromSpottings(beaconDistance: BeaconDistance) {

  def calculate(
    agents: PositionedAgents,
    spottings: BeaconSpottings): PositionedBeacon = {

    val distances = spottings.history.map { case (agent, timedRssi) =>
      val dist = beaconDistance.distanceToBeacon(BeaconSpotting(spottings.beacon, agent, timedRssi.head))
      agent + " -> " + dist.coord + "m"
    }.mkString("; ")

    println(distances)

    null
  }
}
