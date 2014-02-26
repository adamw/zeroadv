package zeroadv.db

import zeroadv.{Agent, ReceivedAdv}
import reactivemongo.bson.{BSONDocument, BSONDocumentWriter, BSONDocumentReader}
import org.joda.time.DateTime
import akka.actor.ActorSystem
import reactivemongo.core.commands.LastError
import scala.concurrent.Future

class EventCollection(mongoDb: MongoDb, system: ActorSystem) {
  import system.dispatcher

  private val coll = mongoDb.coll("event")

  private def init() {}
  init()

  private implicit object ReceivedAdvReader extends BSONDocumentReader[ReceivedAdv] {
    def read(bson: BSONDocument) = {
      ReceivedAdv(
        bson.getAs[DateTime]("when").get,
        Agent(bson.getAs[String]("agent").get),
        bson.getAs[Array[Byte]]("adv").get,
        bson.getAs[Int]("rssi").get
      )
    }
  }

  private implicit object ReceivedAdvWriter extends BSONDocumentWriter[ReceivedAdv] {
    def write(adv: ReceivedAdv) = BSONDocument(
      "when" -> adv.when,
      "agent" -> adv.agent.name,
      "adv" -> adv.adv,
      "rssi" -> adv.rssi
    )
  }

  def write(adv: ReceivedAdv): Future[LastError] = {
    coll.insert(adv, mongoDb.writeConcern)
  }

  def find(): Future[List[ReceivedAdv]] = {
    coll.find(BSONDocument()).cursor[ReceivedAdv].collect[List]()
  }
}
