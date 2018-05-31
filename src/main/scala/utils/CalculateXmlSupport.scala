package utils

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import org.scalactic.{Bad, Good}
import services.web.CalculateRequest
import utils.Maybe.Maybe

import scala.xml.NodeSeq

trait CalculateXmlSupport
  extends ScalaXmlSupport {
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
          <message>{"Web Service Error"}</message>
          <error>{t.map(_.getMessage).mkString(";")}</error>
        </res>
    }
  }


  implicit def calculateReqFromXml(xmlReq: NodeSeq): CalculateRequest = {
    CalculateRequest(
      (xmlReq \ "v2").text.toInt,
      (xmlReq \ "v3").text.toInt,
      (xmlReq \ "v4").text.toInt
    )
  }

}