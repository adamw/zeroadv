package zeroadv

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

case class ReceivedAdv(when: DateTime, agent: String, adv: Array[Byte], rssi: Int) {
  override def toString = {
    val advStr = adv.map(toUnsigned).map(Integer.toHexString)
      .map(_.toUpperCase).map(x => if (x.length < 2) "0" + x else x)
      .toList.mkString(" ")

    val whenStr = DateTimeFormat.mediumTime().print(when)

    s"$agent@[$whenStr]: $advStr, RSSI = $rssi"
  }
}
