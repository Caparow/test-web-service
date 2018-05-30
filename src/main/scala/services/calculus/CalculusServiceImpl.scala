package services.calculus

import com.google.inject.{Inject, Singleton}
import utils.Maybe.Maybe


@Singleton
class CalculusServiceImpl @Inject()()
  extends CalculusService {

  override def calculate(id: Long): Maybe[Boolean] = ???

  override def getValue(id: Long): Maybe[Double] = ???
}
