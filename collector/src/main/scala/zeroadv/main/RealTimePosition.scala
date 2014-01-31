package zeroadv.main

import zeroadv.{ZeroadvSubscriber, PositioningModule}
import akka.actor.{ActorSystem, Props}

object RealTimePosition extends App {
  val modules = new PositioningModule {
    lazy val system = ActorSystem()
    lazy val positioningActor = system.actorOf(Props(newBeaconPositioningActor(_ => ())))
    lazy val zeroadvSubscriber = new ZeroadvSubscriber(positioningActor ! _)
  }

  modules.zeroadvSubscriber.subscribe(allPis: _*)
}
