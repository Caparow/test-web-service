package services.csv

import utils.Maybe.Maybe

trait CsvService {
  def readValueFromFile1(id: Int): Maybe[Double]

  def readValueFromFile2(id: Int): Maybe[Double]

  def writeValueToFile2(id: Int, value: Double): Maybe[Double]
}
