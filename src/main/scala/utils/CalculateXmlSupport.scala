package utils

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import org.scalactic.{Bad, Good}
import services.web.CalculateRequest
import utils.Maybe._

import scala.util.Try
import scala.xml.NodeSeq

trait CalculateXmlSupport
  extends ScalaXmlSupport {
  final val errorMessage = "Web Service Error"

  implicit def maybeBoolToString(b: Maybe[Boolean]): Maybe[String] = b.map(bb => if (bb) "1" else "0")

  implicit def xmlToStr(n: NodeSeq): String = n.toString

  implicit def boolToXml(b: Maybe[Boolean]): String = resToXml(b)

  implicit def resToXml(maybeDouble: Maybe[String]): String = {
    maybeDouble match {
      case Good(b) =>
        <res>
          <value>{b}</value>
        </res>
      case Bad(t) =>
        <res>
          <message>{errorMessage}</message>
          <error>{t.map(_.getMessage).mkString(";")}</error>
        </res>
    }
  }

  def responseFromXml(xmlResponse: NodeSeq): String = {
    val n = xmlResponse \ "value"
    if ((xmlResponse \ "value").length > 0)
      n.text
    else
      (xmlResponse \ "message").text
  }

  implicit def calculateReqFromXml(xmlReq: NodeSeq): Maybe[CalculateRequest] = {
    Try(CalculateRequest(
      (xmlReq \ "v2").text.toInt,
      (xmlReq \ "v3").text.toInt,
      (xmlReq \ "v4").text.toInt
    ))
  }

}