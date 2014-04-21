package zeroadv.position.nn

import zeroadv.{PosM, TimedRssi, Agent}

case class TrainingExample(input: Map[Agent, List[TimedRssi]], output: PosM) {
  def inputToDoubleArray = NN.inputToDoubleArray(input)
  def outputToDimMArray = Array(output.x, output.y)
}
