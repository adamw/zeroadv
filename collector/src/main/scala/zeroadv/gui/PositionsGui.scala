package zeroadv.gui

import scala.swing._
import zeroadv._
import akka.actor.{Props, ActorSystem}
import zeroadv.position.PositionModule

object PositionsGui extends SimpleSwingApplication with PositionModule {
  lazy val system = ActorSystem()
  lazy val agents = PositionedAgents(List(
    PositionedAgent(Agent("a"), PosM(DimM(2), DimM(2)))
  ))

  lazy val drawPanel = new DrawPanel(PosM(DimM(-1), DimM(-1)), PosM(DimM(9), DimM(9)), 400, 400)
  drawPanel.updateAgents(agents)

  lazy val positioningActor = system.actorOf(Props(newBeaconPositioningActor(drawPanel.updateBeacon)))
  positioningActor ! agents

  lazy val zeroadvSubscriber = new ZeroadvSubscriber(positioningActor ! _)

  lazy val mainFrame = new GuiMainFrame(drawPanel, system)

  override def top = mainFrame
}
