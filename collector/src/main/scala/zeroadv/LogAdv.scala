package zeroadv

object LogAdv extends App {
  new ZeroadvSubscriber(println).subscribe("tcp://pi1:8916")
}
