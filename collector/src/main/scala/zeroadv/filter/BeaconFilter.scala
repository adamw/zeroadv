package zeroadv.filter

import zeroadv.BeaconSpotting

trait BeaconFilter {
  lazy val includeBeaconSpotting = (bs: BeaconSpotting) => true
}
