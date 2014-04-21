package zeroadv.position.nn

import zeroadv.DimM

class NNOutputScaling(minCoord: Double, maxCoord: Double) {
  private val minScaled = 0.0d
  private val maxScaled = 1.0d

  def scaleFromCoord(coord: DimM): Double = (coord.coord - minCoord) / (maxCoord - minCoord) * (maxScaled - minScaled) + minScaled
  def scaleToCoord(scaled: Double): DimM = DimM((scaled - minScaled) / (maxScaled - minScaled) * (maxCoord - minCoord) + minCoord)
}
