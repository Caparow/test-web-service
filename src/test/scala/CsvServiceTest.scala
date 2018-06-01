import com.google.inject.Guice.createInjector
import config.CsvServiceConfig
import modules.{ConfigLoader, CsvServiceModule}
import org.scalatest._
import services.csv.CsvService


class CsvServiceTest
  extends FlatSpec
    with RandomData
    with Matchers {

  private val injector = createInjector(
    new CsvServiceModule,
    new ConfigLoader
  )

  private val csvService = injector.getInstance(classOf[CsvService])
  private val config = injector.getInstance(classOf[CsvServiceConfig])

  import csvService._

  "writeToFile" should "return correct value for writing non empty list" in {
    val list = fillList(1)
    val writed = writeToFile(list, config.dir + config.file1)
    assert(writed.isGood)
    assert(list == writed.get)
  }

  "readWholeFile" should "return error for reading from not existing file" in {
    assert(readWholeFile("some").isBad)
  }

  it should "return error for reading empty file" in {
    writeToFile(List.empty, config.dir + config.file1)
    assert(readWholeFile(config.dir + config.file1).isBad)
  }

  it should "return error for corrupted values in file" in {
    writeToFile(List("s"), config.dir + config.file1)
    assert(readWholeFile(config.dir + config.file1).isBad)
  }

  it should "return correct result for reading existed non empty file" in {
    val list = fillList(10)
    writeToFile(list, config.dir + config.file1)
    val read =readWholeFile(config.dir + config.file1)
    assert(read.isGood)
    assert(read.get == list)
  }

  "readValueFromFile" should "return error for reading value out of list bounds" in {
    val list = fillList(1)
    writeToFile(list, config.dir + config.file1)
    assert(readValueFromFile1(2).isBad)
  }

  it should "return correct and same value by index, from list that was written before" in {
    val list = fillList(10)
    writeToFile(list, config.dir + config.file1)
    val read = readValueFromFile1(2)
    assert(read.isGood)
    assert(read.get == list(2))
  }

  "writeValueToFile2" should "return error for out of index error while writing" in {
    val list = fillList(1)
    writeToFile(list, config.dir + config.file2)
    assert(writeValueToFile2(4, 1.0).isBad)
  }

  it should "return correct result, for writing, with the same value" in {
    val list = fillList(10)
    writeToFile(list, config.dir + config.file2)
    assert(writeValueToFile2(2, 1.0).isGood)
    assert(writeValueToFile2(2, 1.0).get == 1.0)
  }
}
