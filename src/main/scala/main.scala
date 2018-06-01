import com.google.inject.Guice.createInjector
import modules._
import services.web.WebService


object main extends App {
  override def main(args: Array[String]): Unit = {

    val injector = createInjector(
      new CsvServiceModule,
      new ConfigLoader,
      new CalculusServiceModule,
      new WebServiceModule)

    val webService = injector.getInstance(classOf[WebService])
    webService.startApplication()
  }
}
