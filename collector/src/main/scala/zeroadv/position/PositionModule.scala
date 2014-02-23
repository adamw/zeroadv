package zeroadv.position

import zeroadv._

trait PositionModule {
  lazy val beaconDistance = wire[BeaconDistance]
  lazy val beaconPosFromSpottings = wire[BeaconPosFromSpottings]
  lazy val receivedAdvParser = wire[ReceivedAdvParser]
  lazy val calculatePosition = wire[CalculatePosition]
  lazy val includeBeaconSpotting = (bs: BeaconSpotting) => true
  def newBeaconPositioningActor(positionedBeaconSink: PositionedBeacon => Any) = wire[BeaconPositioningActor]
}
