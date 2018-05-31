import com.google.inject.{Guice, Inject, Singleton}
import config.CsvServiceConfig
import modules.{CalculusServiceModule, ConfigLoader, CsvServiceModule, WebServiceModule}
import services.csv.{CsvService, CsvServiceImpl}
import services.web.WebService


object Main extends App  {
  override def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(
      new CsvServiceModule,
      new ConfigLoader,
      new CalculusServiceModule,
      new WebServiceModule
    )
    val c = injector.getInstance(classOf[WebService])
    c.startApplication
  }
}
