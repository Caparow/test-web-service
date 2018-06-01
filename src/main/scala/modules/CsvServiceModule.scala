package modules

import com.google.inject.AbstractModule
import services.csv.{CsvService, CsvServiceImpl}

class CsvServiceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[CsvService]).to(classOf[CsvServiceImpl])
  }
}