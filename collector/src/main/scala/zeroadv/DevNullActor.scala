package zeroadv

import akka.actor.Actor

class DevNullActor extends Actor {
  override def receive = {
    case _ =>
  }
}
