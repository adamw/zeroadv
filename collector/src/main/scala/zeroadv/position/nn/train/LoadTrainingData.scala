package zeroadv.position.nn.train

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import zeroadv._
import zeroadv.BeaconSpotting
import zeroadv.ReceivedAdv
import zeroadv.EndMark
import zeroadv.MarkPosition
import com.typesafe.scalalogging.slf4j.Logging
import zeroadv.position.ReceivedAdvParser
import zeroadv.db.EventCollection
import zeroadv.position.nn.NNConfig

class LoadTrainingData(
  receivedAdvParser: ReceivedAdvParser,
  eventCollection: EventCollection,
  includeBeaconSpotting: BeaconSpotting => Boolean,
  nnConfig: NNConfig) extends Logging {

  def load(): Iterable[TrainingExample] = {
    val allEvents = Await.result(eventCollection.find(), Duration.Inf)
    logger.info("Number of events: " + allEvents.size)

    val positionsToSpottings = allEvents
      .sortBy(_.when.getMillis)
      .foldLeft((Option.empty[PosM], Map[PosM, List[BeaconSpotting]]())) { case ((currentPos, acc), event) =>
      (currentPos, event) match {
        case (Some(pos), ev: ReceivedAdv) =>
          receivedAdvParser.parse(ev) match {
            case Some(bs) if includeBeaconSpotting(bs) =>
              val updated = pos -> (bs :: acc.getOrElse(pos, Nil))
              (Some(pos), acc + updated)
            case _ => (Some(pos), acc)
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

    val allExamples = positionsToSpottings.flatMap { case (pos, spottings) =>
      spottings.foldLeft((BeaconsSpottings(Map()), List[TrainingExample]())) { case ((beaconsSpottings, acc), spotting) =>
        val (beaconSpottings, newBeaconsSpottings) = beaconsSpottings.addSpotting(spotting, nnConfig.spottingsPerAgent)
        val newExample = if (beaconSpottings.history.size == nnConfig.agentsCount && beaconSpottings.history.forall(_._2.size == nnConfig.spottingsPerAgent)) {
          Some(TrainingExample(beaconSpottings.history, pos))
        } else {
          None
        }
        (newBeaconsSpottings, newExample.map(_ :: acc).getOrElse(acc))
      }._2
    }

    logger.info("Number of training examples: " + allExamples.size)

    allExamples
  }
}
