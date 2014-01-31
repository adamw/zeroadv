package zeroadv

trait PositioningModule {
  lazy val beaconDistance = wire[BeaconDistance]
  lazy val beaconPosFromSpottings = wire[BeaconPosFromSpottings]
  lazy val receivedAdvParser = wire[ReceivedAdvParser]
  def newBeaconPositioningActor(positinedBeaconSink: PositionedBeacon => Any) = wire[BeaconPositioningActor]
}
