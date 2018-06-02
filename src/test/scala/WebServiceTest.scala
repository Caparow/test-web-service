import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.google.inject.Guice.createInjector
import config.CsvServiceConfig
import modules.{CalculusServiceModule, ConfigLoader, CsvServiceModule, WebServiceModule}
import org.scalactic.Good
import org.scalatest.{Assertion, FlatSpec, Matchers}
import services.calculus.CalculusService
import services.csv.CsvService
import services.web.{CalculateRequest, WebService}
import utils.CalculateXmlSupport

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.xml.NodeSeq

class WebServiceTest
  extends FlatSpec
    with RandomData
    with Matchers
    with ScalatestRouteTest
    with CalculateXmlSupport
    with ScalaXmlSupport {

  private val injector = createInjector(
    new CsvServiceModule,
    new ConfigLoader,
    new CalculusServiceModule,
    new WebServiceModule
  )

  private val csvService = injector.getInstance(classOf[CsvService])
  private val calculusService = injector.getInstance(classOf[CalculusService])
  private val webService = injector.getInstance(classOf[WebService])
  private val config = injector.getInstance(classOf[CsvServiceConfig])

  import webService._

  "Web Service" should "response with error/bad status for invalid or not supported request" in {
    Get("/rest/calc/s") ~> Route.seal(route) ~> check {
      status shouldEqual StatusCodes.MethodNotAllowed
    }
    Get("/rest/calc/") ~> Route.seal(route) ~> check {
      status shouldEqual StatusCodes.MethodNotAllowed
    }
    HttpRequest(HttpMethods.POST, uri = "/rest/calc") ~> route ~> check {
      responseFromXml(responseAs[NodeSeq]) shouldEqual errorMessage
    }
    HttpRequest(HttpMethods.POST, uri = "/rest") ~> Route.seal(route) ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  it should "handle few connections in time and response correctly for writing/reading file" in {
    val client = new TestClient()
    val listLength = 100
    val list1 = fillList(listLength)
    val list2 = fillList(listLength)
    csvService.writeToFile(list1, config.dir + config.file1)
    csvService.writeToFile(list2, config.dir + config.file2)

    def sendReq(): Assertion = {
      val readList1 = csvService.readWholeFile(config.dir + config.file1).get
      val readList2 = csvService.readWholeFile(config.dir + config.file2).get
      val req = generateRequest(calculusService.threshold, readList1, readList2)
      Await.result(client.queueRequest(createRequest(req._1)).map { res =>
        res.entity shouldEqual HttpEntity(ContentTypes.`text/xml(UTF-8)`, Good(req._2))
      }, 3.seconds)
    }

    (1 to client.QueueSize).par.foreach { _ =>
      sendReq()
    }
  }

  "GET request to /rest/calc/{v1}" should "response with correct XML with value" in {
    val list2 = fillList(10)
    csvService.writeToFile(list2, config.dir + config.file2)
    Get("/rest/calc/2") ~> route ~> check {
      responseFromXml(responseAs[NodeSeq]) shouldEqual calculusService.processValue(list2(2)).toString
    }
  }

  it should "response with error message for index out of bounds" in {
    val list2 = fillList(1)
    csvService.writeToFile(list2, config.dir + config.file2)
    Get("/rest/calc/2") ~> route ~> check {
      responseFromXml(responseAs[NodeSeq]) shouldEqual errorMessage
    }
  }

  it should "response with error message for empty file" in {
    csvService.writeToFile(List.empty, config.dir + config.file2)
    Get("/rest/calc/2") ~> route ~> check {
      responseFromXml(responseAs[NodeSeq]) shouldEqual errorMessage
    }
  }

  "POST /rest/calc/" should "return correct XML response" in {
    val list1 = fillList(100)
    val list2 = fillList(100)
    csvService.writeToFile(list1, config.dir + config.file1)
    csvService.writeToFile(list2, config.dir + config.file2)
    val req = generateRequest(calculusService.threshold, list1, list2)
    1 to 100 foreach { _ =>
      for {
        readList1 <- csvService.readWholeFile(config.dir + config.file1)
        readList2 <- csvService.readWholeFile(config.dir + config.file1)
      } {
        val req = generateRequest(calculusService.threshold, readList1, readList2)
        createRequest(req._1) ~> route ~> check {
          responseFromXml(responseAs[NodeSeq]) shouldEqual maybeBoolToString(Good(req._2)).get
        }
      }
    }
  }

  it should "return correct XML error for out of bounds or empty files requests" in {
    val list1 = fillList(100)
    val list2 = fillList(100)

    def assertReq() = {
      createRequest(CalculateRequest(1, 2, 2)) ~> route ~> check {
        responseFromXml(responseAs[NodeSeq]) shouldEqual errorMessage
      }
    }

    def checkOnList(list: List[Double]) = {
      csvService.writeToFile(list, config.dir + config.file1)
      csvService.writeToFile(list2, config.dir + config.file2)
      assertReq()
      csvService.writeToFile(list1, config.dir + config.file1)
      csvService.writeToFile(list, config.dir + config.file2)
      assertReq()
    }

    checkOnList(List.empty)
    checkOnList(List(1.0))
  }

}