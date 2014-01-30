package zeroadv

import zeroadv.db.DbModule
import akka.actor.{Props, ActorSystem}

object WriteAdvToMongo extends App {
  val modules = new DbModule {
    lazy val system = ActorSystem()

    lazy val writeAdvToMongoActor = system.actorOf(Props(new WriteAdvToMongoActor(advCollection)))
    lazy val zeroadvSubscriber = new ZeroadvSubscriber(writeAdvToMongoActor ! _)
  }

  modules.zeroadvSubscriber.subscribe("tcp://pi1:8916")
}
