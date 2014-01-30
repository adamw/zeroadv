package zeroadv.main

import zeroadv.db.DbModule
import akka.actor.{Props, ActorSystem}
import zeroadv._
import zeroadv.ReceivedAdv

object WriteAdvToMongoAndLog extends App {
  val modules = new DbModule {
    lazy val system = ActorSystem()

    lazy val writeAdvToMongoActor = system.actorOf(Props(new WriteAdvToMongoActor(advCollection)))
    lazy val advSink = (adv: ReceivedAdv) => {
      println(adv)
      writeAdvToMongoActor ! adv
    }
    lazy val zeroadvSubscriber = wire[ZeroadvSubscriber]
  }

  modules.zeroadvSubscriber.subscribe("tcp://pi1:8916")
}
