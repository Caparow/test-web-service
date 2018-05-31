package modules

import com.google.inject.AbstractModule
import config.CsvServiceConfig
import pureconfig._
import pureconfig.error.ConfigReaderFailures

class ConfigLoader extends AbstractModule{
  override def configure(): Unit = {
    val csvServiceConfig = loadConfig[CsvServiceConfig]("services.csv").right.get
    bind(classOf[CsvServiceConfig]).toInstance(csvServiceConfig)
  }
}
