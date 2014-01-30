package zeroadv

import zeroadv.db.DbModule
import akka.actor.{Props, ActorSystem}

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
