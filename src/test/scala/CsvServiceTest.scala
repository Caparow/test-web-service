import com.google.inject.Guice.createInjector
import config.CsvServiceConfig
import modules.{CalculusServiceModule, ConfigLoader, CsvServiceModule, WebServiceModule}
import org.scalactic.{Bad, Every, Good}
import org.scalatest.FlatSpec
import services.csv.CsvService


class CsvServiceTest
  extends FlatSpec
    with RandomData {

  private val injector = createInjector(
    new CsvServiceModule,
    new ConfigLoader,
    new CalculusServiceModule,
    new WebServiceModule)

  private val csvService = injector.getInstance(classOf[CsvService])
  private val config = injector.getInstance(classOf[CsvServiceConfig])
  import csvService._

  "writeToFile" should "return Good[List[Double]] for writing non empty list" in {
    val list = fillList(1)
    assert(writeToFile(list, config.dir + config.file1).isInstanceOf[Good[List[Double]]])
  }

  "readWholeFile" should "return Bad[Every[Throwable]] for reading from not existing file" in {
    assert(readWholeFile("some").isInstanceOf[Bad[Every[Throwable]]])
  }

  it should "return Bad[Every[Throwable]] for reading empty file" in {
    writeToFile(List.empty, config.dir + config.file1)
    assert(readWholeFile(config.dir + config.file1).isInstanceOf[Bad[Every[Throwable]]])
  }

  it should "return Good[List[Double]] for reading existed non empty file" in {
    val list = fillList(10)
    writeToFile(list, config.dir + config.file1)
    assert(readWholeFile(config.dir + config.file1).isInstanceOf[Good[List[Double]]])
  }

  it should "read and return the same list that was written before" in {
    val list = fillList(10)
    writeToFile(list, config.dir + config.file1)
    assert(readWholeFile(config.dir + config.file1).get == list)
  }

  "readValueFromFile" should "return Bad[Every[Throwable]] for reading value out of list bounds" in {
    val list = fillList(1)
    writeToFile(list, config.dir + config.file1)
    assert(readValueFromFile1(2).isInstanceOf[Bad[Every[Throwable]]])
  }

  it should "read and return the same value by index, from list that was written before" in {
    val list = fillList(10)
    writeToFile(list, config.dir + config.file1)
    assert(readValueFromFile1(2).get == list(2))
  }

  "writeValueToFile2" should "return Bad[Every[Throwable]] for out of index error while writing" in {
    val list = fillList(1)
    writeToFile(list, config.dir + config.file1)
    assert(writeValueToFile2(4, 1.0).isInstanceOf[Bad[Every[Throwable]]])
  }

  it should "return Good[Double] for correct writing with the same value" in {
    val list = fillList(10)
    writeToFile(list, config.dir + config.file1)
    assert(writeValueToFile2(2, 1.0).isInstanceOf[Good[Double]])
    assert(writeValueToFile2(2, 1.0).get == 1.0)
  }
}
