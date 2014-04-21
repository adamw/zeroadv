package zeroadv.gui

import scala.swing.SimpleSwingApplication
import zeroadv._
import akka.actor.{Props, ActorSystem}
import zeroadv.position.PositionModule
import zeroadv.db.DbModule
import zeroadv.zeromq.ZeroadvSubscriber
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.duration._
import zeroadv.filter.IncludeOnlyLightGreenBeacon
import java.awt.Color

object PositionsGui extends SimpleSwingApplication with PositionModule with DbModule
with IncludeOnlyLightGreenBeacon with AgentSetup with Logging {

  lazy val system = ActorSystem()
  import system.dispatcher

  lazy val drawPanel = new DrawPanel(PosM(minDim, minDim), PosM(maxDim, maxDim), 400, 400)
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

  lazy val distanceBasedPositioningActor = system.actorOf(Props(newBeaconPositioningActor(distanceBasedBeaconPos,
    drawPanel.updateBeacon(Color.GREEN, _))))
  lazy val nnBasedPositioningActor = system.actorOf(Props(newBeaconPositioningActor(nnBasedBeaconPos,
    drawPanel.updateBeacon(Color.BLUE, _))))

  distanceBasedPositioningActor ! agents
  nnBasedPositioningActor ! agents

  lazy val enableMongo = true
  lazy val writeEventToMongoActor = if (enableMongo) {
    system.actorOf(Props(newWriteEventToMongoActor))
  } else {
    system.actorOf(Props[DevNullActor])
  }

  lazy val statsActor = system.actorOf(Props(wire[StatsActor]))
  system.scheduler.schedule(1.second, 1.second, statsActor, LogStats)

  lazy val zeroadvSubscriber = new ZeroadvSubscriber({ adv =>
    distanceBasedPositioningActor ! adv
    nnBasedPositioningActor ! adv
    writeEventToMongoActor ! adv
    statsActor ! adv
  })
  zeroadvSubscriber.subscribeAndListInDaemonThread(allPis)

  lazy val mainFrame = new GuiMainFrame(drawPanel, markPanel, system)

  override def top = mainFrame
}
