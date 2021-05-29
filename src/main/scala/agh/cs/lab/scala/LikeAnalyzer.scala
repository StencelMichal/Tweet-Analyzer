package agh.cs.lab.scala

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.util.matching.Regex

object LikeAnalyzer {

  final case class Tweet(tweet: String, likes: Int) extends Message

  final case class SaveData(tweet: String) extends Message

  final val wordMap = collection.mutable.Map[String, (Int, Int)]()

  def apply(): Behavior[Tweet] = Behaviors.receiveMessage { message =>
    for (word <- message.tweet.split("\\s+")) {
      val numberPattern: Regex = "^(?i)[a-ząćęłńśóżź]".r

      numberPattern.findFirstMatchIn(word) match {
        case Some(_) =>
          val result: (Int, Int) = wordMap getOrElse(word, (0, 0))
          wordMap += (Filter.filterPunctuation(word) -> (result._1 + 1, result._2 + message.likes))
        case None =>
      }
    }

    val maxLikes = wordMap.maxBy(obj => 1.0 * obj._2._2 / obj._2._1)
    println(maxLikes._1)
    println(maxLikes._2)
//    for ((tweet, (number, likes)) <- wordMap) {
//      println(tweet, number, likes)
//    }
    //    print(wordMap.keys.size)
    //    wordMap.foreach { case (key: String, value: Int) => println(key + " " + value) }
    //    for ((word, count) <- wordMap) {
    //      println("1")
    //      println(word + " " + count)
    //    }
    Behaviors.same
  }
}
