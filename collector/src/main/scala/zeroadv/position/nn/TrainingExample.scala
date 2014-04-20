package zeroadv.position.nn

import zeroadv.{PosM, TimedRssi, Agent}

case class TrainingExample(input: Map[Agent, List[TimedRssi]], output: PosM) {
  def inputToDoubleArray = input
    .toList
    .sortBy(_._1.name)
    .flatMap(_._2.sortBy(_.when.getMillis))
    .map(_.rssi.toDouble)
    .toArray

  def outputToDimMArray = Array(output.x, output.y)
}
