import com.google.inject.Guice.createInjector
import config.CsvServiceConfig
import modules.{CalculusServiceModule, ConfigLoader, CsvServiceModule}
import org.scalactic.Good
import org.scalatest._
import services.calculus.CalculusService
import services.csv.CsvService
import services.web.CalculateRequest

class CalculusServiceTest
  extends FlatSpec
    with RandomData {

  private val injector = createInjector(
    new CsvServiceModule,
    new ConfigLoader,
    new CalculusServiceModule
  )

  private val csvService = injector.getInstance(classOf[CsvService])
  private val calculusService = injector.getInstance(classOf[CalculusService])
  private val config = injector.getInstance(classOf[CsvServiceConfig])
  import calculusService._

  "getValue" should "return correct result for reading" in {
    val list2 = fillList(100)
    csvService.writeToFile(list2, config.dir + config.file2)
    val indexToReadLessThen10 = list2.indexWhere(_ < 10)
    val indexToReadMoreThen10 = list2.indexWhere(_ > 10)
    val readLess10 = getValue(indexToReadLessThen10)
    val readMore10 = getValue(indexToReadMoreThen10)
    assert(readLess10.isGood)
    assert(readLess10.get == list2(indexToReadLessThen10))
    assert(readMore10.isGood)
    assert(readMore10.get == list2(indexToReadMoreThen10) - calculusService.threshold)
  }

  it should "return error result for reading out of bounds" in {
    val list2 = fillList(1)
    csvService.writeToFile(list2, config.dir + config.file2)
    assert(getValue(4).isBad)
  }

  it should "return error for reading from empty file" in {
    csvService.writeToFile(List.empty, config.dir + config.file2)
    assert(getValue(4).isBad)
  }

  "calculate" should "return correct result for calculating" in {
    val list1 = fillList(100)
    val list2 = fillList(100)
    csvService.writeToFile(list1, config.dir + config.file1)
    csvService.writeToFile(list2, config.dir + config.file2)
    1 to 100 foreach { _ =>
      for {
        readList1 <- csvService.readWholeFile(config.dir + config.file1)
        readList2 <- csvService.readWholeFile(config.dir + config.file2)
      } {
        val req = generateRequest(calculusService.threshold, readList1, readList2)
        val res = calculate(Good(req._1))
        assert(res.isGood)
        assert(res.get == req._2)
      }
    }
  }

  it should "return error from request with out of bounds indexes" in {
    val list1 = fillList(10)
    val list2 = fillList(10)
    csvService.writeToFile(list1, config.dir + config.file1)
    csvService.writeToFile(list2, config.dir + config.file2)
    val req1 = CalculateRequest(10, 15, 6)
    val res1 = calculate(Good(req1))
    val req2 = CalculateRequest(10, 6, 15)
    val res2 = calculate(Good(req2))
    assert(res2.isBad)
    assert(res1.isBad)
  }

  it should "return error for empty files" in {
    val list = fillList(10)
    csvService.writeToFile(list, config.dir + config.file1)
    csvService.writeToFile(List.empty, config.dir + config.file2)
    val req1 = CalculateRequest(10, 6, 6)
    val res1 = calculate(Good(req1))
    csvService.writeToFile(list, config.dir + config.file2)
    csvService.writeToFile(List.empty, config.dir + config.file1)
    val req2 = CalculateRequest(10, 6, 6)
    val res2 = calculate(Good(req2))
    assert(res2.isBad)
    assert(res1.isBad)
  }
}
