package zeroadv.main

import zeroadv.zeromq.ZeroadvSubscriber

object LogAdv extends App {
  new ZeroadvSubscriber(println).subscribeAndListen(allPis)
}
