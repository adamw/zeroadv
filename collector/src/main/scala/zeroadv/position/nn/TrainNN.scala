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
import scala.util.Random
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.BasicNetwork
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation
import org.encog.Encog

object TrainNN extends App with DbModule with IncludeOnlyLightGreenBeacon with NNConfig with AgentSetup with Logging {
  lazy val system = ActorSystem()

  lazy val receivedAdvParser = wire[ReceivedAdvParser]

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

  val minCoord = minDim.coord
  val maxCoord = maxDim.coord

  val minScaled = 0.0d
  val maxScaled = 1.0d

  def scaleFromCoord(coord: DimM): Double = (coord.coord - minCoord) / (maxCoord - minCoord) * (maxScaled - minScaled) + minScaled
  def scaleToCoord(scaled: Double): DimM = DimM((scaled - minScaled) / (maxScaled - minScaled) * (maxCoord - minCoord) + minCoord)

  case class TrainingExample(input: Map[Agent, List[TimedRssi]], output: PosM) {
    def inputToDoubleArray = input
      .toList
      .sortBy(_._1.name)
      .flatMap(_._2.sortBy(_.when.getMillis))
      .map(_.rssi.toDouble)
      .toArray

    def outputToDoubleArray = Array(scaleFromCoord(output.x), scaleFromCoord(output.y))
  }

  val allExamples = positionsToSpottings.flatMap { case (pos, spottings) =>
    spottings.foldLeft((BeaconsSpottings(Map()), List[TrainingExample]())) { case ((beaconsSpottings, acc), spotting) =>
      val (beaconSpottings, newBeaconsSpottings) = beaconsSpottings.addSpotting(spotting, spottingsPerAgent)
      val newExample = if (beaconSpottings.history.size == agents.agents.size && beaconSpottings.history.forall(_._2.size == spottingsPerAgent)) {
        Some(TrainingExample(beaconSpottings.history, pos))
      } else {
        None
      }
      (newBeaconsSpottings, newExample.map(_ :: acc).getOrElse(acc))
    }._2
  }

  logger.info("Number of training examples: " + allExamples.size)

  val random = new Random()
  val (trainingExamples, testExamples) = random.shuffle(allExamples).splitAt((allExamples.size * 0.9).toInt)

  //

  val network = new BasicNetwork()
  network.addLayer(new BasicLayer(null, true, agents.agents.size * spottingsPerAgent))
  network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 20))
  network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 2))
  network.getStructure.finalizeStructure()
  network.reset()

  val trainingSet = new BasicMLDataSet(
    trainingExamples.map(_.inputToDoubleArray).toArray,
    trainingExamples.map(_.outputToDoubleArray).toArray
  )

  val train = new ResilientPropagation(network, trainingSet)

  var epoch = 1

  do {
    train.iteration()
    println("Epoch #" + epoch + " Error:" + train.getError)
    epoch += 1
  } while (epoch <= 1000)
  train.finishTraining()

  for (testExample <- testExamples) {
    val output = Array.ofDim[Double](2)
    network.compute(testExample.inputToDoubleArray, output)
    println("%.4f -> %.4f, %.4f -> %.4f (%.4f, %.4f)".format(
      testExample.output.x.coord, scaleToCoord(output(0)).coord,
      testExample.output.y.coord, scaleToCoord(output(1)).coord,
      output(0), output(1)))
  }

  println("Train error: " + train.getError)
  println("Test error: " + network.calculateError(new BasicMLDataSet(
    testExamples.map(_.inputToDoubleArray).toArray,
    testExamples.map(_.outputToDoubleArray).toArray
  )))

  //

  Encog.getInstance().shutdown()
  system.shutdown()
  system.awaitTermination()
  sys.exit(0)
}
