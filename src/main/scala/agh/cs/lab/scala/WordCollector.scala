package agh.cs.lab.scala

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.util.matching.Regex

object WordCollector {
  final case class Tweet(tweet: String)

  final case class SaveData(tweet: String)

  final val wordMap = collection.mutable.Map[String, Int]()

  def apply(): Behavior[Tweet] = Behaviors.receiveMessage { message =>
    for (word <- message.tweet.split("\\s+")) {
      val numberPattern: Regex = "^(?i)[a-ząćęłńśóżź]".r

      numberPattern.findFirstMatchIn(word) match {
        case Some(_) =>
          val result: Int = wordMap getOrElse(word, 0)
          wordMap += (word.toLowerCase.dropWhile(c => ",.\";\'".indexOf(c) > 0) -> (result + 1))
        case None =>
      }
    }
    //    print(wordMap.keys.size)
    //    wordMap.foreach { case (key: String, value: Int) => println(key + " " + value) }
    //    for ((word, count) <- wordMap) {
    //      println("1")
    //      println(word + " " + count)
    //    }
    Behaviors.same
  }
}