package zeroadv.db

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import zeroadv.PositioningEvent

class WriteEventToMongoActor(eventCollection: EventCollection) extends Actor with Logging {
  import context.dispatcher

  def receive = {
    case ev: PositioningEvent => {
      eventCollection.write(ev).onFailure { case e: Exception =>
        logger.error(s"Cannot write event $ev", e)
      }
    }
  }
}
