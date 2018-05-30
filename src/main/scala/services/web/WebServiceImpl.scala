package services.web

import com.google.inject.{Inject, Singleton}
import services.calculus.CalculusService

@Singleton
class WebServiceImpl @Inject()()(
  calculusService: CalculusService
) extends WebService {

  override def startApplication: Unit = ???

}
