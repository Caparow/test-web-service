import scala.util.Random

trait RandomData {
  def fillList(length: Int): List[Double] = {
    List.fill(length)(Random.nextDouble()*15)
  }
}
