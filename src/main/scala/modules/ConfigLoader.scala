package modules

import com.google.inject.AbstractModule
import config.{CsvServiceConfig, WebServiceConfig}
import pureconfig._

class ConfigLoader
  extends AbstractModule {

  override def configure(): Unit = {
    val csvServiceConfig = loadConfig[CsvServiceConfig]("services.csv").right.get
    bind(classOf[CsvServiceConfig]).toInstance(csvServiceConfig)

    val webServiceConfig = loadConfig[WebServiceConfig]("services.web").right.get
    bind(classOf[WebServiceConfig]).toInstance(webServiceConfig)
  }
}
