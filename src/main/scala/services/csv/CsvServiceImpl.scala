package services.csv

import java.io.File

import com.google.inject.{Inject, Singleton}
import com.github.tototoshi.csv._
import config.CsvServiceConfig
import utils.Maybe._
import scala.util.Try

@Singleton
class CsvServiceImpl @Inject()()(
  config: CsvServiceConfig
) extends CsvService {

  private def readValueAt(id: Int, filename: String): Maybe[Double] = {
    Try {
      val fileReader: CSVReader = CSVReader.open(new File(filename))
      val valueList = fileReader.all().head
      fileReader.close()
      valueList(id).toDouble
    }
  }

  private def writeValueTo(id: Int, value: Double, filename: String): Maybe[Double] = {
    Try {
      val fileReader: CSVReader = CSVReader.open(new File(filename))
      val valueList = fileReader.all()
      fileReader.close()
      val newValueList = valueList.head.updated(id, value)

      val fileWriter: CSVWriter = CSVWriter.open(new File(filename))
      fileWriter.writeAll(List(newValueList))

      fileWriter.close()
      value
    }
  }


  override def readValueFromFile1(id: Int): Maybe[Double] = {
    readValueAt(id, config.dir + config.file1)
  }

  override def readValueFromFile2(id: Int): Maybe[Double] = {
    readValueAt(id, config.dir + config.file2)
  }

  override def writeValueToFile2(id: Int, value: Double): Maybe[Double] = {
    writeValueTo(id, value, config.dir + config.file2)
  }
}
