package agh.cs.lab.scala.utils

object Latinifier {
  final val charMap = Map[Char, Char]('ą' -> 'a',
    'ć' -> 'c',
    'ę' -> 'e',
    'ł' -> 'l',
    'ń' -> 'n',
    'ó' -> 'o',
    'ś' -> 's',
    'ź' -> 'z',
    'ż' -> 'z')

  def replace(character: Char): Char = {
    val result: Option[Char] = charMap.get(character)
    result match {
      case Some(c) => c
      case None => character
    }
  }

  def latinify(word: String): String = {
    word.map(c => replace(c))
  }
}
