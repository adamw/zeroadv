package zeroadv.position.nn

import zeroadv.AgentSetup

trait NNModule extends AgentSetup {
  lazy val nnConfig = NNConfig(agents.agents.size, 4, List(12))
  lazy val nnOutputScaling = new NNOutputScaling(minDim.coord, maxDim.coord)
  def loadNNFromFile = NN.loadFromFile(nnOutputScaling, nnConfig)
}
