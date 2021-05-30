package agh.cs.lab.scala.actors

import agh.cs.lab.scala.utils.{Filter, ResultPrinter}
import agh.cs.lab.scala.actorCommands.{ActorCommand, Print, SaveData, TweetWithLikes}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import spray.json.DefaultJsonProtocol.{IntJsonFormat, StringJsonFormat, jsonFormat1, mapFormat, tuple2Format}
import spray.json.{RootJsonFormat, enrichAny}

import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.immutable.ListMap
import scala.util.matching.Regex

object LikeAnalyzer {

  final val wordMap = collection.mutable.Map[String, (Int, Int)]()

  def save(): Unit = {
    case class Obj(count: Map[String, (Int, Int)])
    implicit val format: RootJsonFormat[Obj] = jsonFormat1(Obj)

    val jsonStr = Obj(ListMap(wordMap.toSeq.sortBy(_._2._2)(Ordering[Int].reverse): _*)).toJson

    ResultPrinter.print("src/main/data/like_analyzer/LikeAnalyzer_" + new SimpleDateFormat("YYYYMMdd_HHmmss").format(new Date) + ".json", jsonStr.toString())
  }

  def printResults(): Unit = {
    val result = new StringBuilder()
    result ++= "\n[Najbardziej like'owane słowa]\n"
    result ++= "   | Słowo | Liczba like'ów: | \n"
    ListMap(wordMap.toSeq.sortBy(_._2._2): _*).slice(0, 10)
      .zipWithIndex foreach { case (el, idx) => result ++= idx.toString + ". " + el._1 + " " + (el._2._2 / el._2._1) + "\n" }
    println(result.toString)
  }

  def apply(): Behavior[ActorCommand] = Behaviors.receiveMessage[ActorCommand] {
    case TweetWithLikes(tweet, likes) =>
      for (word <- tweet.split("\\s+").toSet[String]) {
        val numberPattern: Regex = "^(?i)[a-ząćęłńśóżź]".r

        numberPattern.findFirstMatchIn(word) match {
          case Some(_) =>
            val result: (Int, Int) = wordMap getOrElse(word, (0, 0))
            wordMap += (Filter.filterPunctuation(word) -> (result._1 + 1, result._2 + likes))
          case None =>
        }
      }

      Behaviors.same

    case SaveData() =>
      save()
      Behaviors.same
    case Print() =>
      printResults()
      Behaviors.same
  }
}
