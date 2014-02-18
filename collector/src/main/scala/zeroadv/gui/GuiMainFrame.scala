package zeroadv.gui

import scala.swing.{Panel, Button, BorderPanel, MainFrame}
import BorderPanel.Position._
import akka.actor.ActorSystem
import scala.concurrent.duration._

class GuiMainFrame(drawPanel: Panel, actorSystem: ActorSystem) extends MainFrame {
  title = "Inverse Beacon Positioning"
  contents = new BorderPanel {
    layout(Button("Exit") {
      actorSystem.shutdown()
      actorSystem.awaitTermination(10.seconds)
      sys.exit(0)
    }) = North

    layout(drawPanel) = Center
  }
}
