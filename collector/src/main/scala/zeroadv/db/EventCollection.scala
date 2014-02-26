package zeroadv.db

import zeroadv._
import reactivemongo.bson.{BSONDocument, BSONDocumentWriter, BSONDocumentReader}
import org.joda.time.DateTime
import akka.actor.ActorSystem
import reactivemongo.core.commands.LastError
import scala.concurrent.Future
import zeroadv.ReceivedAdv
import zeroadv.Agent
import zeroadv.MarkPosition

class EventCollection(mongoDb: MongoDb, system: ActorSystem) {
  import system.dispatcher

  private val coll = mongoDb.coll("event")

  private def init() {}
  init()

  private val ReceivedAdvKind = "adv"
  private val MarkPositionKind = "mark"
  private val EndMarkKind = "endmark"

  private implicit object PositioningEventReader extends BSONDocumentReader[PositioningEvent] {
    def read(bson: BSONDocument) = {
      val k = bson.getAs[String]("kind").get
      k match {
        case ReceivedAdvKind =>
          ReceivedAdv(
            bson.getAs[DateTime]("when").get,
            Agent(bson.getAs[String]("agent").get),
            bson.getAs[Array[Byte]]("adv").get,
            bson.getAs[Int]("rssi").get
          )
        case MarkPositionKind =>
          MarkPosition(
            bson.getAs[DateTime]("when").get,
            PosM(DimM(bson.getAs[Double]("x").get), DimM(bson.getAs[Double]("y").get))
          )
        case EndMarkKind =>
          EndMark(bson.getAs[DateTime]("when").get)
      }
    }
  }

  private implicit object ReceivedAdvWriter extends BSONDocumentWriter[ReceivedAdv] {
    def write(adv: ReceivedAdv) = BSONDocument(
      "kind" -> ReceivedAdvKind,
      "when" -> adv.when,
      "agent" -> adv.agent.name,
      "adv" -> adv.adv,
      "rssi" -> adv.rssi
    )
  }

  private implicit object MarkPositionWriter extends BSONDocumentWriter[MarkPosition] {
    def write(mp: MarkPosition) = BSONDocument(
      "kind" -> MarkPositionKind,
      "when" -> mp.when,
      "x" -> mp.pos.x.coord,
      "y" -> mp.pos.y.coord
    )
  }

  private implicit object EndMarkWriter extends BSONDocumentWriter[EndMark] {
    def write(em: EndMark) = BSONDocument(
      "kind" -> EndMarkKind,
      "when" -> em.when
    )
  }

  def write(event: PositioningEvent): Future[LastError] = {
    event match {
      case adv: ReceivedAdv => coll.insert(adv, mongoDb.writeConcern)
      case mp: MarkPosition => coll.insert(mp, mongoDb.writeConcern)
      case em: EndMark => coll.insert(em, mongoDb.writeConcern)
    }
  }

  def find(): Future[List[PositioningEvent]] = {
    coll.find(BSONDocument()).cursor[PositioningEvent].collect[List]()
  }
}
