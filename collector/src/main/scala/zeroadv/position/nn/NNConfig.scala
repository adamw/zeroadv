package zeroadv.position.nn

case class NNConfig(agentsCount: Int, spottingsPerAgent: Int, nnHiddenLayers: List[Int]) {
  def nnInputSize = agentsCount * spottingsPerAgent
}
