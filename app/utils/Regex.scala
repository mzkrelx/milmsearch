package utils

object Regex {

  /** Control Char Regex. Don't use \p{Cntrl} only for US-ASCII. */
  val ctrl = """[\x00-\x1F\x7F]+""".r
}
