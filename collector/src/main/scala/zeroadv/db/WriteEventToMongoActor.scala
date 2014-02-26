package zeroadv.db

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import zeroadv.ReceivedAdv

class WriteEventToMongoActor(eventCollection: EventCollection) extends Actor with Logging {
  import context.dispatcher

  def receive = {
    case adv: ReceivedAdv => {
      eventCollection.write(adv).onFailure { case e: Exception =>
        logger.error("Cannot write adv", e)
      }
    }
  }
}
