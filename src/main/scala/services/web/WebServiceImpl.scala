package services.web

import com.google.inject.{Inject, Singleton}
import services.calculus.CalculusService
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import utils.CalculateXmlSupport

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.xml.NodeSeq

@Singleton
class WebServiceImpl @Inject()()(
  calculusService: CalculusService
) extends WebService
  with CalculateXmlSupport {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val route: Route =
    pathPrefix("rest") {
      pathPrefix("calc") {
        path(IntNumber) {
          num => {
            (get & pathEnd) {
              complete(HttpEntity(ContentTypes.`text/xml(UTF-8)`, calculusService.getValue(num).map(_.toString)))
            }
          }
        } ~ (post & entity(as[NodeSeq]) & pathEnd) {
          calculateReq => {
            complete(HttpEntity(ContentTypes.`text/xml(UTF-8)`, calculusService.calculate(calculateReq)))
          }
        }
      }
    }

  val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(route, "localhost", 8080)


  override def startApplication: Unit = {
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
