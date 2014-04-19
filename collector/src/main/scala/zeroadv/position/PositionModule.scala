package zeroadv.position

import zeroadv._

trait PositionModule {
  lazy val beaconDistance = wire[BeaconDistance]
  lazy val beaconPosFromSpottings = wire[DistanceBasedBeaconPos]
  lazy val receivedAdvParser = wire[ReceivedAdvParser]
  lazy val calculatePosition = wire[CalculatePosition]
  lazy val includeBeaconSpotting = (bs: BeaconSpotting) => true
  lazy val spottingsRssisLimit = 10
  def newBeaconPositioningActor(positionedBeaconSink: PositionedBeacon => Any) = wire[BeaconPositioningActor]
}
