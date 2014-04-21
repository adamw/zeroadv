package zeroadv.position.nn.train

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
import zeroadv.position.nn.{NNModule, NN, NNConfig, NNOutputScaling}

class TrainNN(
  nnOutputScaling: NNOutputScaling,
  nnConfig: NNConfig) extends Logging {

  def train(allExamples: Iterable[TrainingExample]): NN = {
    val random = new Random()
    val (trainingExamples, testExamples) = random.shuffle(allExamples).splitAt((allExamples.size * 0.9).toInt)

    val network = new BasicNetwork()
    network.addLayer(new BasicLayer(null, true, nnConfig.nnInputSize))
    nnConfig.nnHiddenLayers.foreach { count =>
      network.addLayer(new BasicLayer(new ActivationSigmoid(), true, count))
    }
    network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 2))
    network.getStructure.finalizeStructure()
    network.reset()

    val trainingSet = new BasicMLDataSet(
      trainingExamples.map(_.inputToDoubleArray).toArray,
      trainingExamples.map(_.outputToDimMArray.map(nnOutputScaling.scaleFromCoord)).toArray
    )

    val train = new ResilientPropagation(network, trainingSet)

    var epoch = 1
    val maxLastErrors = 100
    val lastErrors = collection.mutable.Queue[Double]()
    do {
      train.iteration()
      val error = train.getError
      logger.info("Epoch #" + epoch + ", error: " + error)
      lastErrors.enqueue(error)
      if (lastErrors.size > maxLastErrors) lastErrors.dequeue()
      epoch += 1
    } while (epoch <= maxLastErrors || lastErrors.max - lastErrors.min > 0.0001)
    train.finishTraining()

    for (testExample <- testExamples) {
      val output = Array.ofDim[Double](2)
      network.compute(testExample.inputToDoubleArray, output)
      logger.debug("%.4f -> %.4f, %.4f -> %.4f (%.4f, %.4f)".format(
        testExample.output.x.coord, nnOutputScaling.scaleToCoord(output(0)).coord,
        testExample.output.y.coord, nnOutputScaling.scaleToCoord(output(1)).coord,
        output(0), output(1)))
    }

    logger.info("Train error: " + train.getError)
    logger.info("Test error: " + network.calculateError(new BasicMLDataSet(
      testExamples.map(_.inputToDoubleArray).toArray,
      testExamples.map(_.outputToDimMArray.map(nnOutputScaling.scaleFromCoord)).toArray
    )))

    new NN(nnOutputScaling, nnConfig, network)
  }
}

object TrainNN extends App with DbModule with IncludeOnlyLightGreenBeacon with AgentSetup with NNModule with Logging {
  lazy val system = ActorSystem()
  lazy val receivedAdvParser = wire[ReceivedAdvParser]
  lazy val loadTrainingData: LoadTrainingData = wire[LoadTrainingData]
  lazy val trainNN: TrainNN = wire[TrainNN]

  lazy val allExamples = loadTrainingData.load()
  val nn = trainNN.train(allExamples)
  nn.saveToFile()

  Encog.getInstance().shutdown()
  system.shutdown()
  system.awaitTermination()
  sys.exit(0)
}
