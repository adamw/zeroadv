package zeroadv.position

import zeroadv._
import zeroadv.filter.BeaconFilter

trait PositionModule extends BeaconFilter {
  lazy val beaconDistance = wire[BeaconDistance]
  lazy val beaconPosFromSpottings = wire[DistanceBasedBeaconPos]
  lazy val receivedAdvParser = wire[ReceivedAdvParser]
  lazy val calculatePosition = wire[CalculatePosition]
  lazy val spottingsRssisLimit = 10
  def newBeaconPositioningActor(positionedBeaconSink: PositionedBeacon => Any) = wire[BeaconPositioningActor]
}
