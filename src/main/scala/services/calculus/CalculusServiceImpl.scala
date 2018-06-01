package services.calculus

import com.google.inject.Inject
import services.csv.CsvService
import services.web.CalculateRequest
import utils.Maybe.Maybe


class CalculusServiceImpl @Inject()(csvService: CsvService)
  extends CalculusService {

  private def adjustFile2Value(init: Double): (Boolean, Double) = {
    if (init < threshold) {
      (true, init + threshold)
    } else {
      (false, init)
    }
  }

  override def processValue(value: Double): Double = {
    if (value > threshold)
      value - threshold
    else
      value
  }

  override def calculate(calculateRequest: Maybe[CalculateRequest]): Maybe[Boolean] = {
    calculateRequest.flatMap { calculateRequest =>
      csvService.readValueFromFile1(calculateRequest.v3).flatMap { value =>
        val valueWithV2 = adjustFile2Value(value + calculateRequest.v2)
        csvService.writeValueToFile2(calculateRequest.v4, valueWithV2._2).map(_ => valueWithV2._1)
      }
    }
  }

  override def getValue(v1: Int): Maybe[Double] = {
    csvService.readValueFromFile2(v1).map(processValue)
  }
}
