package zeroadv.position.nn

import akka.actor.ActorSystem
import zeroadv.db.DbModule
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import zeroadv._
import zeroadv.EndMark
import zeroadv.ReceivedAdv
import zeroadv.filter.IncludeOnlyLightGreenBeacon
import zeroadv.position.ReceivedAdvParser
import com.typesafe.scalalogging.slf4j.Logging

object TrainNN extends App with DbModule with IncludeOnlyLightGreenBeacon with Logging {
  lazy val system = ActorSystem()

  lazy val receivedAdvParser = wire[ReceivedAdvParser]

  val allEvents = Await.result(eventCollection.find(), Duration.Inf)
  logger.info("Number of events: " + allEvents.size)

  val positionsToSpottings = allEvents
    .sortBy(_.when.getMillis)
    .foldLeft((Option.empty[PosM], Map[PosM, List[BeaconSpotting]]())) { case ((currentPos, acc), event) =>
    (currentPos, event) match {
      case (Some(pos), ev: ReceivedAdv) => {
        receivedAdvParser.parse(ev) match {
          case Some(bs) if includeBeaconSpotting(bs) => {
            val updated = pos -> (bs :: acc.getOrElse(pos, Nil))
            (Some(pos), acc + updated)
          }
          case _ => (Some(pos), acc)
        }
      }
      case (Some(pos), ev: EndMark) => (None, acc)
      case (Some(pos), ev: MarkPosition) => throw new IllegalStateException()
      case (None, ev: ReceivedAdv) => (None, acc)
      case (None, ev: EndMark) => throw new IllegalStateException()
      case (None, MarkPosition(_, pos)) => (Some(pos), acc)
    } }
    ._2
    .mapValues(_.reverse)

  logger.info("Spottings per position: ")
  positionsToSpottings.foreach { case (pos, spottings) =>
    logger.info(s"   $pos -> ${spottings.size}")
  }

  system.shutdown()
  system.awaitTermination()
  sys.exit(0)
}
