package zeroadv.gui

import scala.swing.{Action, BorderPanel, Button}
import BorderPanel.Position._

class MarkPanel(markStartAction: () => Unit, markEndAction: () => Unit) extends BorderPanel with Runnable {
  var marked = false
  var markStart = 0L

  private val btn = new Button {
    text = ""
    action = Action("") {
      marked = !marked
      runAction()
      updateText()
    }

    def updateText() {
      if (marked) {
        val sinceStart = (System.currentTimeMillis()-markStart)/1000L
        text = s"Mark end (since start: ${sinceStart}s)"
      } else {
        text = "Mark start"
      }
    }

    def runAction() {
      if (marked) {
        markStartAction()
        markStart = System.currentTimeMillis()
      } else {
        markEndAction()
      }
    }

    updateText()
  }

  layout(btn) = North

  def run() {
    btn.updateText()
    repaint()
  }
}
