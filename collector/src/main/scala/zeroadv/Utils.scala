package zeroadv

import com.softwaremill.macwire.Macwire
import com.softwaremill.debug.DebugConsole

trait Utils extends Macwire with DebugConsole {
  def toUnsigned(b:   Byte): Int = b & 0xFF

  val allPis = List("tcp://pi1:8916", "tcp://pi2:8916", "tcp://pi3:8916")
}

object Utils extends Utils
