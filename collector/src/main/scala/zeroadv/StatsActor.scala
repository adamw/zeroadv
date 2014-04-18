package zeroadv

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging

class StatsActor extends Actor with Logging {
  val agentCounts = new collection.mutable.HashMap[Agent, Int]()

  override def receive = {
    case ReceivedAdv(_, agent, _, _) => {
      agentCounts(agent) = agentCounts.getOrElse(agent, 0) + 1
    }
    case LogStats => {
      val msg = agentCounts.toList.sortBy(_._1.name).map { case (a, c) => s"${a.name} -> $c" }.mkString("; ")
      logger.info(msg)

      agentCounts.clear()
    }
  }
}

case object LogStats
