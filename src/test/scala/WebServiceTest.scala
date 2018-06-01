import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.{OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.google.inject.Guice.createInjector
import config.CsvServiceConfig
import modules.{CalculusServiceModule, ConfigLoader, CsvServiceModule, WebServiceModule}
import org.scalactic.Good
import org.scalatest.{FlatSpec, Matchers}
import services.calculus.CalculusService
import services.csv.CsvService
import services.web.{CalculateRequest, WebService}
import utils.CalculateXmlSupport

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
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

  def requestToXml(req: CalculateRequest): String = {
    xmlToStr(<req>
      <v2>{req.v2}</v2>
      <v3>{req.v3}</v3>
      <v4>{req.v4}</v4>
    </req>)
  }

  def createRequest(req: CalculateRequest): HttpRequest = {
    HttpRequest(HttpMethods.POST,
      uri = "/rest/calc/",
      entity = HttpEntity(ContentTypes.`text/xml(UTF-8)`, requestToXml(req)))
  }

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
    csvService.writeToFile(List.empty, config.dir + config.file1)
    csvService.writeToFile(list2, config.dir + config.file2)
    createRequest(generateRequest(calculusService.threshold, list1, list2)._1) ~> route ~> check {
      responseFromXml(responseAs[NodeSeq]) shouldEqual errorMessage
    }
    csvService.writeToFile(list1, config.dir + config.file1)
    csvService.writeToFile(List.empty, config.dir + config.file2)
    createRequest(generateRequest(calculusService.threshold, list1, list2)._1) ~> route ~> check {
      responseFromXml(responseAs[NodeSeq]) shouldEqual errorMessage
    }
    csvService.writeToFile(list1, config.dir + config.file1)
    csvService.writeToFile(List(1.0), config.dir + config.file2)
    createRequest(CalculateRequest(1,2,2)) ~> route ~> check {
      responseFromXml(responseAs[NodeSeq]) shouldEqual errorMessage
    }
    csvService.writeToFile(List(1.0), config.dir + config.file1)
    csvService.writeToFile(list2, config.dir + config.file2)
    createRequest(CalculateRequest(1,2,2)) ~> route ~> check {
      responseFromXml(responseAs[NodeSeq]) shouldEqual errorMessage
    }
  }

}
