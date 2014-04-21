package zeroadv.position.nn

import org.encog.neural.networks.BasicNetwork
import zeroadv.{PosM, TimedRssi, Agent}
import java.io.{FileInputStream, DataInputStream, DataOutputStream, FileOutputStream}

class NN(nnOutputScaling: NNOutputScaling, nnConfig: NNConfig, basicNetwork: BasicNetwork) {

  def forInput(input: Map[Agent, List[TimedRssi]]): Option[PosM] = {
    val trimmedInput = input.mapValues(_.take(nnConfig.spottingsPerAgent))
    if (trimmedInput.size != nnConfig.agentsCount || trimmedInput.exists(_._2.size != nnConfig.spottingsPerAgent)) {
      None
    } else {
      val output = Array.ofDim[Double](2)
      basicNetwork.compute(NN.inputToDoubleArray(input), output)

      Some(PosM(
        nnOutputScaling.scaleToCoord(output(0)),
        nnOutputScaling.scaleToCoord(output(1))
      ))
    }
  }

  def saveToFile() {
    val encoded = Array.ofDim[Double](basicNetwork.encodedArrayLength)
    basicNetwork.encodeToArray(encoded)

    val dos = new DataOutputStream(new FileOutputStream(NN.FileName))
    dos.writeInt(encoded.size)
    encoded.foreach(dos.writeDouble)
    dos.close()
  }
}

object NN {
  private val FileName = "/Users/adamw/projects/zeroadv/nn.bin"

  def inputToDoubleArray(input: Map[Agent, List[TimedRssi]]) = input
    .toList
    .sortBy(_._1.name)
    .flatMap(_._2.sortBy(_.when.getMillis))
    .map(_.rssi.toDouble)
    .toArray

  def loadFromFile(nnOutputScaling: NNOutputScaling, nnConfig: NNConfig): NN = {
    val dis = new DataInputStream(new FileInputStream(FileName))
    val size = dis.readInt()
    val repr = Array.ofDim[Double](size)
    for (i <- 0 until size) repr(i) = dis.readDouble()
    dis.close()

    val bn = new BasicNetwork()
    bn.decodeFromArray(repr)

    new NN(nnOutputScaling, nnConfig, bn)
  }
}