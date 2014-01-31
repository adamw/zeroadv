package zeroadv

import com.softwaremill.macwire.Macwire
import com.softwaremill.debug.DebugConsole

trait Utils extends Macwire with DebugConsole {
  def toUnsigned(b: Byte): Int = b & 0xFF

  def hexStringToByteArray(s: String) = s.split(" ").map(Integer.parseInt(_, 16).toByte).toArray

  def byteArrayToHexString(b: Array[Byte]) = b.map(toUnsigned).map(Integer.toHexString)
    .map(_.toUpperCase).map(x => if (x.length < 2) "0" + x else x)
    .toList.mkString(" ")

  val allPis = List("tcp://pi1:8916", "tcp://pi2:8916", "tcp://pi3:8916")
}

object Utils extends Utils
