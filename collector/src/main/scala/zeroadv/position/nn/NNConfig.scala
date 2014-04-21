package zeroadv.position.nn

case class NNConfig(agentsCount: Int, spottingsPerAgent: Int, hiddenLayers: List[Int]) {
  def nnInputSize = agentsCount * spottingsPerAgent
}
