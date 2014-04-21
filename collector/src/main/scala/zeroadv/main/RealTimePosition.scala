package zeroadv.main

import akka.actor.{ActorSystem, Props}
import zeroadv.position.PositionModule
import zeroadv.zeromq.ZeroadvSubscriber

object RealTimePosition extends App {
  val modules = new PositionModule {
    lazy val system = ActorSystem()
    lazy val positioningActor = system.actorOf(Props(newBeaconPositioningActor(distanceBasedBeaconPos, _ => ())))
    lazy val zeroadvSubscriber = new ZeroadvSubscriber(positioningActor ! _)
  }

  modules.zeroadvSubscriber.subscribeAndListen(allPis)
}
