package zeroadv.gui

import scala.swing._
import BorderPanel.Position._
import akka.actor.ActorSystem
import scala.concurrent.duration._

class GuiMainFrame(drawPanel: Panel, markPanel: MarkPanel, actorSystem: ActorSystem) extends MainFrame {
  title = "Inverse Beacon Positioning"
  contents = new BorderPanel {
    val top = new BorderPanel {
      layout(Button("Exit") {
        actorSystem.shutdown()
        actorSystem.awaitTermination(10.seconds)
        sys.exit(0)
      }) = North

      layout(markPanel) = South
    }

    layout(top) = North

    layout(drawPanel) = Center
  }
}
