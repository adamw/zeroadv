package zeroadv.gui

import scala.swing._
import zeroadv._
import akka.actor.{Props, ActorSystem}
import zeroadv.position.PositionModule

object PositionsGui extends SimpleSwingApplication with PositionModule {
  lazy val system = ActorSystem()
  lazy val agents = PositionedAgents(List(
    PositionedAgent(Agent("pi1"), PosM(DimM(0), DimM(2.5))),  // pink
    PositionedAgent(Agent("pi2"), PosM(DimM(3), DimM(0))),    // transparent
    PositionedAgent(Agent("pi3"), PosM(DimM(3), DimM(4.5)))   // black
  ))

  override lazy val includeBeaconSpotting = (bs: BeaconSpotting) => bs.beacon match {
    case IBeaconBeacon(_, 43024, _, _) => true
    case _ => false
  }

  lazy val drawPanel = new DrawPanel(PosM(DimM(-1), DimM(-1)), PosM(DimM(5.5), DimM(5.5)), 400, 400)
  drawPanel.updateAgents(agents)

  lazy val positioningActor = system.actorOf(Props(newBeaconPositioningActor(drawPanel.updateBeacon)))
  positioningActor ! agents

  lazy val zeroadvSubscriber = new ZeroadvSubscriber(positioningActor ! _)
  val t = new Thread() {
    override def run() = {

      zeroadvSubscriber.subscribe(allPis: _*)
    }
  }
  t.setDaemon(true)
  t.start()

  lazy val mainFrame = new GuiMainFrame(drawPanel, system)

  override def top = mainFrame
}
