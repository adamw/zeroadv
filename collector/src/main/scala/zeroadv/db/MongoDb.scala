package zeroadv.db

import reactivemongo.api.{DB, MongoDriver}
import akka.actor.ActorSystem
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.core.commands.GetLastError

class MongoDb(db: DB, system: ActorSystem) {
  def coll(name: String): BSONCollection = db(name)

  val writeConcern = GetLastError(j = true)
}

object MongoDb {
  def connect(system: ActorSystem): MongoDb = {
    import system.dispatcher

    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))
    new MongoDb(connection("zeroadv"), system)
  }
}