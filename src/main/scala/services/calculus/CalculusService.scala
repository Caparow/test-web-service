package services.calculus

import services.web.CalculateRequest
import utils.Maybe.Maybe

trait CalculusService {
  def getValue(v1: Int): Maybe[Double]

  def calculate(calculateRequest: CalculateRequest): Maybe[Boolean]
}
