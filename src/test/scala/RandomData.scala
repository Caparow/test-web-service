import services.web.CalculateRequest

import scala.util.Random

trait RandomData {
  import services.calculus.CalculusService
  def fillList(length: Int): List[Double] = {
    List.fill(length)(Random.nextDouble()*15)
  }

  def generateRequest(threshold: Int,list1: List[Double], list2: List[Double]): (CalculateRequest, Boolean) ={
    val v3 = Random.nextInt(list1.length)
    val v4 = Random.nextInt(list2.length)
    val v2 = Random.nextInt(threshold + threshold/2)
    val req = CalculateRequest(v2,v3,v4)
    (req, (list1(v3)+v2) < threshold)
  }
}
