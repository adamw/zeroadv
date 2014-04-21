package zeroadv.position

import zeroadv._
import zeroadv.filter.BeaconFilter
import zeroadv.position.nn.{NNModule, NNOutputScaling, NN, NNBasedBeaconPos}

trait PositionModule extends BeaconFilter with NNModule {
  lazy val beaconDistance = wire[BeaconDistance]

  //lazy val beaconPosFromSpottings: BeaconPosFromSpottings = wire[DistanceBasedBeaconPos]

  lazy val beaconPosFromSpottings: BeaconPosFromSpottings = new NNBasedBeaconPos(loadNNFromFile)

  lazy val receivedAdvParser = wire[ReceivedAdvParser]
  lazy val calculatePosition = wire[CalculatePosition]
  lazy val spottingsRssisLimit = 10
  def newBeaconPositioningActor(positionedBeaconSink: PositionedBeacon => Any) = wire[BeaconPositioningActor]
}
