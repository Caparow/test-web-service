package services.calculus

import utils.Maybe.Maybe

trait CalculusService {
  def getValue(id: Long): Maybe[Double]

  def calculate(id: Long): Maybe[Boolean]
}
