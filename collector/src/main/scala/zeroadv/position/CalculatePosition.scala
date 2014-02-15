package zeroadv.position

import zeroadv.{DimM, PosM}
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.{MultiDirectionalSimplex, SimplexOptimizer}
import org.apache.commons.math3.optim.nonlinear.scalar.{GoalType, ObjectiveFunction}
import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.optim.{MaxEval, InitialGuess}
import com.typesafe.scalalogging.slf4j.Logging

class CalculatePosition extends Logging {
  def calculate(data: List[(PosM, DimM)]): (PosM, DimM) = {
    val xs = data.map(_._1.x.coord)
    val ys = data.map(_._1.y.coord)
    val maxCoord = List(xs.min, xs.max, ys.min, ys.max).map(math.abs).max
    def randomCoord = math.random*maxCoord*2-maxCoord

    val result = new SimplexOptimizer(1e-3, 1e-6).optimize(
      new ObjectiveFunction(scoreFunction(data)),
      GoalType.MINIMIZE,
      new MultiDirectionalSimplex(2),
      new InitialGuess(Array(randomCoord, randomCoord)),
      new MaxEval(1000)
    )

    val resultPosM = PosM(DimM(result.getPoint.apply(0)), DimM(result.getPoint.apply(1)))
    val resultDistance = DimM(result.getValue)

    logger.debug(s"Position calculation result from $data: $resultPosM - $resultDistance")

    (resultPosM, resultDistance)
  }

  private def scoreFunction(data: List[(PosM, DimM)]) = new MultivariateFunction() {
    def value(point: Array[Double]) = {
      val p = PosM(DimM(point(0)), DimM(point(1)))

      data
        .map { case (c, r) => math.pow((PosM.dist(c, p) - r).coord, 2) }
        .sum
    }
  }
}

/*
    val opt = new CMAESOptimizer(Integer.MAX_VALUE, Double.NegativeInfinity, true, 1, 1, new JDKRandomGenerator(), false,
      new SimpleValueChecker(1E-3, 1E-6))
    val r2 = opt.optimize(new ObjectiveFunction(scoreFunction(data)), GoalType.MINIMIZE,
      new CMAESOptimizer.Sigma(Array.fill(2)(5.0d)),
      new SimpleBounds(Array(-100.0d, -100.0d), Array(100.0d, 100.0d)),
      new InitialGuess(Array(0.0d, 0.0d)),
      new MaxEval(10000),
      new CMAESOptimizer.PopulationSize(25))
 */
