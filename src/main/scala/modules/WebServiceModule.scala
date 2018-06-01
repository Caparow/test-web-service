package modules

import com.google.inject.AbstractModule
import services.web.{WebService, WebServiceImpl}

class WebServiceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[WebService]).to(classOf[WebServiceImpl])
  }
}