package zeroadv.gui

import scala.swing.SimpleSwingApplication
import zeroadv._
import akka.actor.{Props, ActorSystem}
import zeroadv.position.PositionModule
import zeroadv.db.DbModule
import zeroadv.zeromq.ZeroadvSubscriber

object PositionsGui extends SimpleSwingApplication with PositionModule with DbModule {
  lazy val system = ActorSystem()
  lazy val agents = PositionedAgents(List(
    PositionedAgent(Agent("pi1"), PosM(DimM(0), DimM(2.5))),  // pink
    PositionedAgent(Agent("pi2"), PosM(DimM(3), DimM(0))),    // transparent
    PositionedAgent(Agent("pi3"), PosM(DimM(3), DimM(4.5)))   // black
  ))

  override lazy val includeBeaconSpotting = (bs: BeaconSpotting) => bs.beacon match {
    case IBeaconBeacon(_, 43024, _, _) => true                // light blue
    case _ => false
  }

  lazy val drawPanel = new DrawPanel(PosM(DimM(-1), DimM(-1)), PosM(DimM(5.5), DimM(5.5)), 400, 400)
  drawPanel.updateAgents(agents)

  lazy val positioningActor = system.actorOf(Props(newBeaconPositioningActor(drawPanel.updateBeacon)))
  positioningActor ! agents

  lazy val writeAdvToMongoActor = system.actorOf(Props(newWriteEventToMongoActor))

  lazy val zeroadvSubscriber = new ZeroadvSubscriber({ adv =>
    positioningActor ! adv
    writeAdvToMongoActor ! adv
  })
  zeroadvSubscriber.subscribeAndListInDaemonThread(allPis)

  lazy val mainFrame = new GuiMainFrame(drawPanel, system)

  override def top = mainFrame
}
