package agh.cs.lab.scala

object Filter {
  final val punctuation = ",.\";:\'!?".toSet
  def filterPunctuation(str :String) : String = {
    str.toLowerCase.filterNot(punctuation)
  }
}
