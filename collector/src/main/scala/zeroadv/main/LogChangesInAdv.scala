package zeroadv.main

import zeroadv.ZeroadvSubscriber

object LogChangesInAdv extends App {
  private var last: List[Byte] = Nil

  new ZeroadvSubscriber({ adv =>
    if (last != adv.adv.toList) {
      last = adv.adv.toList
      println(adv.whenStr + ": " + adv.advStr)
    }
  }).subscribe(allPis: _*)
}
