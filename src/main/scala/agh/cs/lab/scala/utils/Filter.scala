package agh.cs.lab.scala.utils

object Filter {
  final val punctuation = ",.\";:\'!?".toSet

  def filterPunctuation(str: String): String = {
    str.toLowerCase.filterNot(punctuation)
  }
}
