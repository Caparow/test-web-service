package services.csv

import com.google.inject.{Inject, Singleton}
import utils.Maybe.Maybe

@Singleton
class CsvServiceImpl @Inject()()(
                              config: Cs
) extends CsvService {

  override def readValueAt(id: Long): Maybe[Double] = ???

  override def writeValueTo(id: Long, value: Double): Maybe[Double] = ???
}
