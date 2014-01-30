package zeroadv

import com.softwaremill.macwire.Macwire
import com.softwaremill.debug.DebugConsole

trait Utils extends Macwire with DebugConsole {
  def toUnsigned(b:   Byte): Int = b & 0xFF
}

object Utils extends Utils
