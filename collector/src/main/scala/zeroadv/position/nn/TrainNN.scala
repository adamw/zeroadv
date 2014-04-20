package zeroadv.position.nn

import akka.actor.ActorSystem
import zeroadv.db.DbModule
import zeroadv._
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

class TrainNN(minCoord: Double, maxCoord: Double, inputLayer: Int, hiddenLayers: List[Int]) {
  private val minScaled = 0.0d
  private val maxScaled = 1.0d

  private def scaleFromCoord(coord: DimM): Double = (coord.coord - minCoord) / (maxCoord - minCoord) * (maxScaled - minScaled) + minScaled
  private def scaleToCoord(scaled: Double): DimM = DimM((scaled - minScaled) / (maxScaled - minScaled) * (maxCoord - minCoord) + minCoord)

  def train(allExamples: Iterable[TrainingExample]) {
    val random = new Random()
    val (trainingExamples, testExamples) = random.shuffle(allExamples).splitAt((allExamples.size * 0.9).toInt)

    val network = new BasicNetwork()
    network.addLayer(new BasicLayer(null, true, inputLayer))
    hiddenLayers.foreach { count =>
      network.addLayer(new BasicLayer(new ActivationSigmoid(), true, count))
    }
    network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 2))
    network.getStructure.finalizeStructure()
    network.reset()

    val trainingSet = new BasicMLDataSet(
      trainingExamples.map(_.inputToDoubleArray).toArray,
      trainingExamples.map(_.outputToDimMArray.map(scaleFromCoord)).toArray
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
      testExamples.map(_.outputToDimMArray.map(scaleFromCoord)).toArray
    )))

    Encog.getInstance().shutdown()
  }
}

object TrainNN extends App with DbModule with IncludeOnlyLightGreenBeacon with NNConfig with AgentSetup with Logging {
  lazy val system = ActorSystem()

  lazy val receivedAdvParser = wire[ReceivedAdvParser]

  lazy val loadTrainingData = new LoadTrainingData(receivedAdvParser, eventCollection, includeBeaconSpotting,
    spottingsPerAgent, agents.agents.size)

  lazy val trainNN = new TrainNN(minDim.coord, maxDim.coord, agents.agents.size * spottingsPerAgent, List(18))

  val allExamples = loadTrainingData.load()

  trainNN.train(allExamples)

  system.shutdown()
  system.awaitTermination()
  sys.exit(0)
}
