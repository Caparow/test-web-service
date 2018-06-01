package services.web

import akka.http.scaladsl.server.Route

trait WebService {
  val route: Route
  def startApplication(): Unit
}
