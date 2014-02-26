package zeroadv.main

import zeroadv.db.DbModule
import akka.actor.ActorSystem
import scala.concurrent.Await
import scala.concurrent.duration._

object ReadEvents extends App {
  val modules = new DbModule {
    lazy val system = ActorSystem()
  }

  Await.result(modules.eventCollection.find(), 10.seconds).foreach(println)

  modules.mongoDb.close()
  modules.system.shutdown()
  modules.system.awaitTermination()
}
