package services.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import config.WebServiceConfig
import services.calculus.CalculusService
import utils.CalculateXmlSupport

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.xml.NodeSeq

class WebServiceImpl @Inject()(calculusService: CalculusService,
                               webServiceConfig: WebServiceConfig)
  extends WebService
    with CalculateXmlSupport {

  implicit val system: ActorSystem = ActorSystem("test-web-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit def myRejectionHandler = RejectionHandler.newBuilder()
    .handleAll[MethodRejection] { methodRejections =>
    val names = methodRejections.map(_.supported.name)
    complete((MethodNotAllowed, s"Can't do that! Supported: ${names mkString " or "}!"))
  }
    .handleNotFound {
      complete((NotFound, "Not here!"))
    }
    .result()

  override val route: Route =
    pathPrefix("rest") {
      pathPrefix("calc") {
        path(IntNumber) {
          num => {
            (get & pathEnd) {
              complete(HttpEntity(ContentTypes.`text/xml(UTF-8)`, calculusService.getValue(num).map(_.toString)))
            }
          }
        } ~ (post & entity(as[NodeSeq]) & pathEndOrSingleSlash) {
          calculateReq => {
            complete(HttpEntity(ContentTypes.`text/xml(UTF-8)`, calculusService.calculate(calculateReq)))
          }
        }
      }
    }

  val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(route, webServiceConfig.interface, webServiceConfig.port)

  override def startApplication: Unit = {
    println(s"Server online at http://${webServiceConfig.interface}:${webServiceConfig.port}\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
