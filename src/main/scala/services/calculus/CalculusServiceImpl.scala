package services.calculus

import com.google.inject.{Inject, Singleton}
import services.csv.CsvService
import services.web.CalculateRequest
import utils.Maybe.Maybe


@Singleton
class CalculusServiceImpl @Inject()()(
  csvService: CsvService
)
  extends CalculusService {

  override def calculate(calculateRequest: CalculateRequest): Maybe[Boolean] = {
    csvService.readValueFromFile1(calculateRequest.v3).flatMap { value =>
      val res: Boolean = (value + calculateRequest.v2) < 10
      if (res)
        csvService.writeValueToFile2(calculateRequest.v4, value + calculateRequest.v2 + 10).map(_ => res)
      else
        csvService.writeValueToFile2(calculateRequest.v4, value + calculateRequest.v2).map(_ => res)
    }
  }

  override def getValue(v1: Int): Maybe[Double] = {
    csvService.readValueFromFile2(v1).map { value =>
      if (value > 10)
        value - 10
      else
        value
    }
  }
}
