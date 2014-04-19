package zeroadv.position

import zeroadv.{PositionedBeacon, BeaconSpottings, PositionedAgents}

trait BeaconPosFromSpottings {
  def calculate(agents: PositionedAgents, spottings: BeaconSpottings): PositionedBeacon
}
