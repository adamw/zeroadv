package zeroadv.main

import zeroadv.{ZeroadvSubscriber}
import akka.actor.{ActorSystem, Props}
import zeroadv.position.PositionModule

object RealTimePosition extends App {
  val modules = new PositionModule {
    lazy val system = ActorSystem()
    lazy val positioningActor = system.actorOf(Props(newBeaconPositioningActor(_ => ())))
    lazy val zeroadvSubscriber = new ZeroadvSubscriber(positioningActor ! _)
  }

  modules.zeroadvSubscriber.subscribe(allPis: _*)
}
