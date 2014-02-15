package zeroadv.position

import org.scalatest.{FlatSpec, ShouldMatchers}
import zeroadv.{DimM, PosM}

class CalculatePositionTest extends FlatSpec with ShouldMatchers {
  // http://www.wolframalpha.com/ formula:
  // min(sqrt((x+20)^2+y^2)-10)^2+(sqrt((x-10)^2+(y-10)^2)-10)^2+(sqrt(x^2+(y+40)^2)-20)^2

  // list of paris of: input data (circle centers, radius), expected sum of squared errors
  val testDatas: List[(List[(PosM, DimM)], Double)] = List(
    (List( // optimal: (10, 0)
      dataPoint(0, 0, 10),
      dataPoint(20, 0, 10),
      dataPoint(10, 10, 10)
    ), 0.0d),
    (List( // optimal: (0, -15)
      dataPoint(0, 0, 10),
      dataPoint(0, -30, 10),
      dataPoint(5, -15, 5)
    ), 25+25+0),
    (List( // optimal: (-2.824, -7.350)
      dataPoint(-20, 0, 10),
      dataPoint(0, -40, 20),
      dataPoint(10, 10, 10)
    ), 372.487d)
  )

  for (testData <- testDatas) {
    it should s"the position from ${testData._1} should be ${testData._2}" in {
      // when
      val result = new CalculatePosition().calculate(testData._1)

      // then
      info("Result: " + result)
      math.abs(result._2.coord - testData._2) should be < 0.1
    }
  }

  def dataPoint(x: Double, y: Double, d: Double) = (PosM(DimM(x), DimM(y)), DimM(d))
  def point(x: Double, y: Double) = PosM(DimM(x), DimM(y))
}
