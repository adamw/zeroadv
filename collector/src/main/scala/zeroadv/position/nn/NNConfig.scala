package zeroadv.position.nn

case class NNConfig(agentsCount: Int, spottingsPerAgent: Int) {
  def nnInputSize = agentsCount * spottingsPerAgent
}
