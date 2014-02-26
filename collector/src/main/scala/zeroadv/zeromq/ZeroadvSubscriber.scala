package zeroadv.zeromq

import org.zeromq.ZMQ
import com.typesafe.scalalogging.slf4j.Logging
import org.joda.time.DateTime
import zeroadv._
import zeroadv.ReceivedAdv

class ZeroadvSubscriber(sink: ReceivedAdv => Any) extends Logging {
  def subscribeAndListen(addresses: List[String]) {
    val context = ZMQ.context(1)

    //  Socket to talk to clients
    val sub = context.socket(ZMQ.SUB)
    addresses.foreach(sub.connect)
    sub.subscribe(new Array[Byte](0))
    logger.info("Connected to: " + addresses.toList)

    while (!Thread.currentThread().isInterrupted) {
      val agent = sub.recvStr()
      val advBytes = sub.recv()
      val rssiBytes = sub.recv()

      val rssi = toUnsigned(rssiBytes(0))-256

      sink(ReceivedAdv(new DateTime(), Agent(agent), advBytes, rssi))
    }

    sub.close()
    context.term()
  }
  
  def subscribeAndListInDaemonThread(addresses: List[String]) {
    val t = new Thread() {
      override def run() = {
        subscribeAndListen(addresses)
      }
    }
    t.setDaemon(true)
    t.start()
  }
}
