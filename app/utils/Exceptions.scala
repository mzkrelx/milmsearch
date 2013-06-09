package utils

case class BadRequestException(msg: String) extends Exception(msg)
