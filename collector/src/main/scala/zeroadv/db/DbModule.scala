package zeroadv.db

import akka.actor.ActorSystem

trait DbModule {
  lazy val mongoDb = MongoDb.connect(system)
  lazy val eventCollection = wire[EventCollection]
  def newWriteEventToMongoActor = wire[WriteEventToMongoActor]

  def system: ActorSystem
}
