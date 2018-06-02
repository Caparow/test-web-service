import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import services.web.CalculateRequest
import utils.CalculateXmlSupport

import scala.util.Random

trait RandomData extends CalculateXmlSupport {
  def fillList(length: Int): List[Double] = {
    List.fill(length)(Random.nextDouble() * 15)
  }

  def generateRequest(threshold: Int, list1: List[Double], list2: List[Double]): (CalculateRequest, Boolean) = {
    val v3 = Random.nextInt(list1.length)
    val v4 = Random.nextInt(list2.length)
    val v2 = Random.nextInt(threshold + threshold / 2)
    val req = CalculateRequest(v2, v3, v4)
    (req, (list1(v3) + v2) < threshold)
  }

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
}
