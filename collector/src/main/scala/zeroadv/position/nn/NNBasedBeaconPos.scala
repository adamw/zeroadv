package zeroadv.position.nn

import zeroadv.position.BeaconPosFromSpottings
import zeroadv._
import zeroadv.PositionedAgents
import zeroadv.PositionedBeacon
import scala.Some
import zeroadv.BeaconSpottings

class NNBasedBeaconPos(nn: NN) extends BeaconPosFromSpottings {
  def calculate(agents: PositionedAgents, spottings: BeaconSpottings) = {
    nn.forInput(spottings.history) match {
      case Some(pos) => PositionedBeacon(spottings.beacon, pos)
      case None => PositionedBeacon(spottings.beacon, PosM(DimM(0), DimM(0)))
    }
  }
}
