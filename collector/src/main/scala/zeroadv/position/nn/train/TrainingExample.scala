package zeroadv.position.nn.train

import zeroadv.{PosM, TimedRssi, Agent}
import zeroadv.position.nn.NN

case class TrainingExample(input: Map[Agent, List[TimedRssi]], output: PosM) {
  def inputToDoubleArray = NN.inputToDoubleArray(input)
  def outputToDimMArray = Array(output.x, output.y)
}
