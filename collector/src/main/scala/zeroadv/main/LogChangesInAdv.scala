package zeroadv.main

import zeroadv.zeromq.ZeroadvSubscriber

object LogChangesInAdv extends App {
  private var last: List[Byte] = Nil

  new ZeroadvSubscriber({ adv =>
    if (last != adv.adv.toList) {
      last = adv.adv.toList
      println(adv.whenStr + ": " + adv.advStr)
    }
  }).subscribeAndListen(allPis)
}
