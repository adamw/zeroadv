package zeroadv.position

import zeroadv._
import zeroadv.filter.BeaconFilter
import zeroadv.position.nn.{NNModule, NNBasedBeaconPos}

trait PositionModule extends BeaconFilter with NNModule {
  lazy val beaconDistance = wire[BeaconDistance]

  lazy val distanceBasedBeaconPos: BeaconPosFromSpottings = wire[DistanceBasedBeaconPos]
  lazy val nnBasedBeaconPos: BeaconPosFromSpottings = new NNBasedBeaconPos(loadNNFromFile)

  lazy val receivedAdvParser = wire[ReceivedAdvParser]
  lazy val calculatePosition = wire[CalculatePosition]
  lazy val spottingsRssisLimit = 10

  def newBeaconPositioningActor(
    beaconPosFromSpottings: BeaconPosFromSpottings,
    positionedBeaconSink: PositionedBeacon => Any) = wire[BeaconPositioningActor]
}
