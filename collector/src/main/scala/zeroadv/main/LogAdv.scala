package zeroadv.main

import zeroadv.ZeroadvSubscriber

object LogAdv extends App {
  new ZeroadvSubscriber(println).subscribe(allPis: _*)
}
