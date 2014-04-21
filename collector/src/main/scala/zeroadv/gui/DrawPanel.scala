package zeroadv.gui

import zeroadv._
import scala.swing._
import java.awt.{Dimension, Color}
import zeroadv.PositionedAgents
import zeroadv.PositionedAgent
import zeroadv.DimM
import scala.swing.event.MouseClicked

class DrawPanel(bottomLeft: PosM, topRight: PosM, width: Int, height: Int) extends Panel {
  private val widthM = topRight.x - bottomLeft.x
  private val heightM = topRight.y - bottomLeft.y

  private val scaleX = width/widthM.coord
  private val scaleY = height/heightM.coord

  private def dimMToWidth(dim: DimM): Int = (dim.coord*scaleX).toInt
  private def dimMToHeight(dim: DimM): Int = (dim.coord*scaleY).toInt

  private var _lastClick: Option[PosM] = None
  def lastClick = _lastClick

  private def posMToPoint(pos: PosM): (Int, Int) = {
    val relative = pos - bottomLeft
    (dimMToWidth(relative.x), height-dimMToHeight(relative.y))
  }

  private def pointToPosM(x: Double, y: Double): PosM = PosM(
    DimM(x/width*widthM.coord) + bottomLeft.x,
    DimM((height-y)/height*heightM.coord) + bottomLeft.y
  )

  private var coloredBeacons = Map[Color, Map[Beacon, PosM]]()
  private var agents = PositionedAgents(Nil)

  override protected def paintComponent(g: Graphics2D) = {
    paintAgents(g)
    paintBeacons(g)
    lastClick.foreach(rectAtPoint(g, _, Color.YELLOW))
  }

  private def paintAgents(g: Graphics2D) {
    agents.agents.foreach(paintAgent(g, _))
  }

  private def paintAgent(g: Graphics2D, agent: PositionedAgent) {
    rectAtPoint(g, agent.pos, Color.RED)
  }

  private def rectAtPoint(g: Graphics2D, center: PosM, color: Color) {
    g.setColor(color)

    val (x, y) = posMToPoint(center)
    val w = 20
    val h = 20

    g.fillRect(x - w/2, y - h/2, w, h)
  }

  private def paintBeacons(g: Graphics2D) {
    for {
      (color, beacons) <- coloredBeacons
      (beacon, pos) <- beacons
    } {
      paintBeacon(g, beacon, pos, color)
    }
  }

  private def paintBeacon(g: Graphics2D, beacon: Beacon, posM: PosM, color: Color) {
    g.setColor(color)

    val (x, y) = posMToPoint(posM)
    val r = 20

    g.fillOval(x-r/2, y-r/2, r, r)
  }

  preferredSize = new Dimension(width, height)

  def updateBeacon(color: Color, positionedBeacon: PositionedBeacon) {
    Swing.onEDT {
      val currentBeaconsForColor = coloredBeacons.getOrElse(color, Map[Beacon, PosM]())
      coloredBeacons = coloredBeacons + (color ->
        (currentBeaconsForColor + (positionedBeacon.beacon -> positionedBeacon.pos)))
      repaint()
    }
  }

  def updateAgents(_agents: PositionedAgents) {
    Swing.onEDT {
      agents = _agents
      repaint()
    }
  }

  reactions += {
    case c: MouseClicked => {
      _lastClick = Some(pointToPosM(c.point.getX, c.point.getY))
      repaint()
    }
  }

  listenTo(mouse.clicks)
}
