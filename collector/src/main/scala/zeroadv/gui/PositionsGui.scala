package zeroadv.gui

import scala.swing.SimpleSwingApplication
import zeroadv._
import akka.actor.{Props, ActorSystem}
import zeroadv.position.PositionModule
import zeroadv.db.DbModule
import zeroadv.zeromq.ZeroadvSubscriber
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.duration._

object PositionsGui extends SimpleSwingApplication with PositionModule with DbModule with Logging {
  lazy val system = ActorSystem()
  import system.dispatcher

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

  lazy val markPanel = new MarkPanel(
    () => drawPanel.lastClick.foreach { pos =>
      logger.info(s"Mark at: $pos")
      writeEventToMongoActor ! MarkPosition(now(), pos)
    },
    () => {
      logger.info("End mark")
      writeEventToMongoActor ! EndMark(now())
    }
  )
  system.scheduler.schedule(1.second, 1.second, markPanel)

  lazy val positioningActor = system.actorOf(Props(newBeaconPositioningActor(drawPanel.updateBeacon)))
  positioningActor ! agents

  lazy val enableMongo = false
  lazy val writeEventToMongoActor = if (enableMongo) {
    system.actorOf(Props(newWriteEventToMongoActor))
  } else {
    system.actorOf(Props[DevNullActor])
  }

  lazy val statsActor = system.actorOf(Props(wire[StatsActor]))
  system.scheduler.schedule(1.second, 1.second, statsActor, LogStats)

  lazy val zeroadvSubscriber = new ZeroadvSubscriber({ adv =>
    positioningActor ! adv
    writeEventToMongoActor ! adv
    statsActor ! adv
  })
  zeroadvSubscriber.subscribeAndListInDaemonThread(allPis)

  lazy val mainFrame = new GuiMainFrame(drawPanel, markPanel, system)

  override def top = mainFrame
}
