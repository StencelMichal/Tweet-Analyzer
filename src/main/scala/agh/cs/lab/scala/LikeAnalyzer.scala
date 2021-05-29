package agh.cs.lab.scala

import agh.cs.lab.scala.WordCollector.wordMap
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import spray.json.DefaultJsonProtocol.{IntJsonFormat, StringJsonFormat, jsonFormat1, mapFormat, tuple2Format}
import spray.json.{RootJsonFormat, enrichAny}

import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.immutable.ListMap
import scala.util.matching.Regex

object LikeAnalyzer {

  final case class Tweet(tweet: String, likes: Int) extends Message

  final case class SaveData() extends Operation

  final case class Stop() extends Operation

  final val wordMap = collection.mutable.Map[String, (Int, Int)]()

  def save(): Unit = {
    case class Obj(count: Map[String, (Int, Int)])
    implicit val format: RootJsonFormat[Obj] = jsonFormat1(Obj)

    val jsonStr = Obj(ListMap(wordMap.toSeq.sortBy(_._2._2):_*)).toJson

    ResultPrinter.print("src/main/data/like_analyzer/LikeAnalyzer_"+ new SimpleDateFormat("YYYYMMdd_HHmmss").format(new Date) +".json", jsonStr.toString())
  }

  def apply(): Behavior[ActorCommand] = Behaviors.receiveMessage[ActorCommand] {
    case Tweet(tweet, likes) =>
      for (word <- tweet.split("\\s+")) {
        val numberPattern: Regex = "^(?i)[a-ząćęłńśóżź]".r

        numberPattern.findFirstMatchIn(word) match {
          case Some(_) =>
            val result: (Int, Int) = wordMap getOrElse(word, (0, 0))
            wordMap += (Filter.filterPunctuation(word) -> (result._1 + 1, result._2 + likes))
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

        case SaveData() =>
          save()
        Behaviors.same
    //    case Stop() =>
  }
}
