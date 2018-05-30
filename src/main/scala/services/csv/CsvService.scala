package services.csv

import utils.Maybe.Maybe

trait CsvService {
  def readValueAt(id: Long): Maybe[Double]

  def writeValueTo(id: Long, value: Double): Maybe[Double]
}
