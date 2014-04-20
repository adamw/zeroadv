package zeroadv

trait AgentSetup {
  lazy val agents = PositionedAgents(List(
    PositionedAgent(Agent("pi1"), PosM(DimM(0), DimM(2.5))),  // pink
    PositionedAgent(Agent("pi2"), PosM(DimM(3), DimM(0))),    // transparent
    PositionedAgent(Agent("pi3"), PosM(DimM(3), DimM(4.5)))   // black
  ))

  lazy val minDim = DimM(-1)
  lazy val maxDim = DimM(5.5)
}
