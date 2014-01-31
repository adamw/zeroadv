package zeroadv.position

import zeroadv.{IBeaconBeacon, GimbalBeacon, DimM, BeaconSpotting}

class BeaconDistance {
  def distanceToBeacon(spotting: BeaconSpotting): DimM = {
    val txPower = spotting.beacon match {
      case g: GimbalBeacon => -69
      case IBeaconBeacon(_, _, _, tp) => tp
    }

    DimM(distanceTo(spotting.rssi.rssi, txPower))
  }

  // from http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing
  private def distanceTo(rssi: Int, txPower: Int) = {
    val ratio = rssi.toDouble*1.0d/txPower.toDouble
    if (ratio < 1.0) {
      Math.pow(ratio, 10)
    }
    else {
      val accuracy =  0.89976d*Math.pow(ratio, 7.7095d) + 0.111d
      accuracy
    }
  }
}
