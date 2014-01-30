package zeroadv.db

import akka.actor.ActorSystem

trait DbModule {
  lazy val mongoDb = MongoDb.connect(system)
  lazy val advCollection = wire[AdvCollection]

  def system: ActorSystem
}
