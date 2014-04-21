package zeroadv.position.nn

import org.encog.neural.networks.BasicNetwork
import zeroadv.{PosM, TimedRssi, Agent}
import java.io.File
import org.encog.persist.EncogDirectoryPersistence

class NN(nnOutputScaling: NNOutputScaling, nnConfig: NNConfig, basicNetwork: BasicNetwork) {

  def forInput(input: Map[Agent, List[TimedRssi]]): Option[PosM] = {
    NN.trimAndValidateInput(nnConfig, input).map { trimmedInput =>
      val output = Array.ofDim[Double](2)
      basicNetwork.compute(NN.inputToDoubleArray(trimmedInput), output)

      PosM(
        nnOutputScaling.scaleToCoord(output(0)),
        nnOutputScaling.scaleToCoord(output(1))
      )
    }
  }

  def saveToFile() {
    EncogDirectoryPersistence.saveObject(new File(NN.FileName), basicNetwork)
  }
}

object NN {
  private val FileName = "/Users/adamw/projects/zeroadv/nn.eg"

  def trimAndValidateInput(nnConfig: NNConfig, input: Map[Agent, List[TimedRssi]]): Option[Map[Agent, List[TimedRssi]]] = {
    val trimmedInput = input.mapValues(_.take(nnConfig.spottingsPerAgent))
    if (trimmedInput.size != nnConfig.agentsCount || trimmedInput.exists(_._2.size != nnConfig.spottingsPerAgent)) {
      None
    } else {
      Some(trimmedInput)
    }
  }

  def inputToDoubleArray(input: Map[Agent, List[TimedRssi]]) = input
    .toList
    .sortBy(_._1.name)
    .flatMap(_._2.sortBy(_.when.getMillis))
    .map(_.rssi.toDouble)
    .toArray

  def loadFromFile(nnOutputScaling: NNOutputScaling, nnConfig: NNConfig): NN = {
    val bn = EncogDirectoryPersistence.loadObject(new File(FileName)).asInstanceOf[BasicNetwork]
    new NN(nnOutputScaling, nnConfig, bn)
  }
}