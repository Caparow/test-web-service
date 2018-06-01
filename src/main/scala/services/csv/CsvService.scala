package services.csv

import utils.Maybe.Maybe

trait CsvService {
  def readValueFromFile1(id: Int): Maybe[Double]

  def readWholeFile(filename: String): Maybe[List[Double]]

  def writeToFile[t](l: List[t], filename: String): Maybe[List[t]]

  def readValueFromFile2(id: Int): Maybe[Double]

  def writeValueToFile2(id: Int, value: Double): Maybe[Double]
}
