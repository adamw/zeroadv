package zeroadv.filter

import zeroadv.{IBeaconBeacon, BeaconSpotting}

trait IncludeOnlyLightGreenBeacon extends BeaconFilter {
  override lazy val includeBeaconSpotting = (bs: BeaconSpotting) => bs.beacon match {
    case IBeaconBeacon(_, 43024, _, _) => true                // light green
    case _ => false
  }
}
