package zeroadv

import akka.actor.Actor
import zeroadv.db.AdvCollection
import com.typesafe.scalalogging.slf4j.Logging

class WriteAdvToMongoActor(advCollection: AdvCollection) extends Actor with Logging {
  import context.dispatcher

  def receive = {
    case adv: ReceivedAdv => {
      advCollection.write(adv).onFailure { case e: Exception =>
        logger.error("Cannot write adv", e)
      }
    }
  }
}
