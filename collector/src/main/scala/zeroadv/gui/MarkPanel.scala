package zeroadv.gui

import scala.swing.{Swing, Action, BorderPanel, Button}
import BorderPanel.Position._

class MarkPanel(markStartAction: () => Unit, markEndAction: () => Unit) extends BorderPanel with Runnable {
  var marked = false
  var markStart = 0L

  private val btn = new Button {
    text = ""
    action = Action("") {
      runAction()
      updateText()
    }

    def runAction() {
      marked = !marked
      if (marked) {
        markStartAction()
        markStart = System.currentTimeMillis()
      } else {
        markEndAction()
      }
    }

    def updateText() {
      if (marked) {
        val sinceStart = secondsSinceStart()
        text = s"Mark end (since start: ${sinceStart}s)"
      } else {
        text = "Mark start"
      }
    }
    
    def finishIfAfter60Seconds() {
      if (marked && secondsSinceStart() >= 60) {
        runAction()
      }

      updateText()
    }
    
    private def secondsSinceStart() = (System.currentTimeMillis()-markStart)/1000

    updateText()
  }

  layout(btn) = North

  def run() {
    Swing.onEDT {
      btn.finishIfAfter60Seconds()
      repaint()
    }
  }
}
