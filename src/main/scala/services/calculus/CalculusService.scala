package services.calculus

import services.web.CalculateRequest
import utils.Maybe.Maybe

trait CalculusService {
  val threshold = 10

  def getValue(v1: Int): Maybe[Double]

  def calculate(calculateRequest: CalculateRequest): Maybe[Boolean]
}
