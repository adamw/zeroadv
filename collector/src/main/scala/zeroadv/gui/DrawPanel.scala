package zeroadv.gui

import zeroadv._
import scala.swing._
import java.awt.{Dimension, Color}
import zeroadv.PositionedAgents
import zeroadv.PositionedAgent
import zeroadv.DimM

class DrawPanel(bottomLeft: PosM, topRight: PosM, width: Int, height: Int) extends Panel {
  private val widthM = topRight.x - bottomLeft.x
  private val heightM = topRight.y - bottomLeft.y

  private val scaleX = width/widthM.coord
  private val scaleY = height/heightM.coord

  private def dimMToWidth(dim: DimM): Int = (dim.coord*scaleX).toInt
  private def dimMToHeight(dim: DimM): Int = (dim.coord*scaleY).toInt

  private def posMToPoint(pos: PosM): (Int, Int) = {
    val relative = pos - bottomLeft
    (dimMToWidth(relative.x), height-dimMToHeight(relative.y))
  }

  private var beacons = Map[Beacon, PosM]()
  private var agents = PositionedAgents(Nil)

  override protected def paintComponent(g: Graphics2D) = {
    paintAgents(g)
    paintBeacons(g)
  }

  private def paintAgents(g: Graphics2D) {
    agents.agents.foreach(paintAgent(g, _))
  }

  private def paintAgent(g: Graphics2D, agent: PositionedAgent) {
    g.setColor(Color.RED)

    val (x, y) = posMToPoint(agent.pos)
    val w = 20
    val h = 20

    g.fillRect(x - w/2, y - h/2, w, h)
  }

  private def paintBeacons(g: Graphics2D) {
    beacons.foreach { case (b, p) => paintBeacon(g, b, p) }
  }

  private def paintBeacon(g: Graphics2D, beacon: Beacon, posM: PosM) {
    g.setColor(Color.BLUE)

    val (x, y) = posMToPoint(posM)
    val r = 20

    g.fillOval(x-r/2, y-r/2, r, r)
  }

  preferredSize = new Dimension(width, height)

  def updateBeacon(positionedBeacon: PositionedBeacon) {
    beacons = beacons + (positionedBeacon.beacon -> positionedBeacon.pos)
    repaint()
  }

  def updateAgents(_agents: PositionedAgents) {
    agents = _agents
    repaint()
  }
}