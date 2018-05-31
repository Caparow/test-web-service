package modules

import com.google.inject.AbstractModule
import services.calculus.{CalculusService, CalculusServiceImpl}

class CalculusServiceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[CalculusService]).to(classOf[CalculusServiceImpl])
  }
}