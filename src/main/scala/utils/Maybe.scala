package utils

import org.scalactic._

import scala.util.{Failure, Success, Try}

object Maybe {

  type Maybe[+T] = Or[T, Every[ServiceException]]

  implicit def toMaybe[T](tryOption: Try[T]): Maybe[T] = tryOption match {
    case Success(res) => Good(res)
    case Failure(t) => Bad(One(new ServiceException(t.getMessage)))
  }
}
