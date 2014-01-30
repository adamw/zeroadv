package zeroadv.main

import zeroadv.ZeroadvSubscriber

object LogAdv extends App {
  new ZeroadvSubscriber(println).subscribe("tcp://pi1:8916")
}

object LogChangesInAdv extends App {
  private var last: List[Byte] = Nil

  new ZeroadvSubscriber({ adv =>
    if (last != adv.adv.toList) {
      last = adv.adv.toList
      println(adv.whenStr + ": " + adv.advStr)
    }
  }).subscribe("tcp://pi1:8916")
}
