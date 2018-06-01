package services.csv

import java.io.File

import com.github.tototoshi.csv._
import com.google.inject.Inject
import config.CsvServiceConfig
import org.scalactic.{Bad, Good, One}
import utils.Maybe._
import utils.ServiceException

import scala.util.{Failure, Success, Try}

class CsvServiceImpl @Inject()(config: CsvServiceConfig)
  extends CsvService {

  private def readValueAt(id: Int, filename: String): Maybe[Double] = {
    readWholeFile(filename).flatMap { list =>
      list.lift(id) match {
        case Some(e) => Good(e)
        case None => Bad(One(new ServiceException("Index out of bounds.")))
      }
    }
  }

  override def readWholeFile(filename: String): Maybe[List[Double]] = {
    Try(CSVReader.open(new File(filename))).flatMap { fileReader =>
      val listOpt = fileReader.all().headOption
      fileReader.close()
      listOpt.map(_.map(_.toDouble)) match {
        case Some(e) => Success(e)
        case None => Failure(new ServiceException("Empty file."))
      }
    }
  }

  override def writeToFile[t](l: List[t], filename: String): Maybe[List[t]] = synchronized {
    Try(CSVWriter.open(new File(filename))).map {
      fileWriter =>
        fileWriter.writeAll(List(l))
        fileWriter.close()
        l
    }
  }

  private def writeValueTo(id: Int, value: Double, filename: String): Maybe[Double] = {
    readWholeFile(filename).flatMap { list =>
      if (list.length - 1 >= id) {
        writeToFile(list.updated(id, value), filename).map(_ => value)
      } else {
        Bad(One(new ServiceException("Index out of bounds.")))
      }
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
