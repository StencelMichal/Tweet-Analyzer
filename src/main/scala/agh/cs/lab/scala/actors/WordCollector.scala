package agh.cs.lab.scala.actors

import agh.cs.lab.scala.utils.{Filter, ResultPrinter}
import agh.cs.lab.scala.actorCommands.{ActorCommand, Print, SaveData, Stop, Tweet}
import akka.actor.typed.scaladsl.Behaviors
import spray.json.DefaultJsonProtocol.{IntJsonFormat, StringJsonFormat, jsonFormat1, mapFormat}
import spray.json.{RootJsonFormat, enrichAny}

import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.immutable.ListMap
import scala.util.matching.Regex

object WordCollector {

  final val wordMap = collection.mutable.Map[String, Int]()

  def save(): Unit = {
    case class Obj(count: Map[String, Int])
    implicit val format: RootJsonFormat[Obj] = jsonFormat1(Obj)
    val jsonStr = Obj(ListMap(wordMap.toSeq.sortBy(_._2)(Ordering[Int].reverse): _*)).toJson

    ResultPrinter.print("src/main/data/word_collector/WordCollector_" + new SimpleDateFormat("YYYYMMdd_HHmmss").format(new Date) + ".json", jsonStr.toString())
  }

  def printResults(): Unit = {
    val result = new StringBuilder()
    result ++= "\n[Najpopularniejsze słowa]\n"
    result ++= "   | Słowo | Liczba wystąpień: | \n"
    ListMap(wordMap.toSeq.sortBy(_._2): _*).slice(0, 10)
      .zipWithIndex foreach { case (el, idx) => result ++= idx.toString + ". " + el._1 + " " + el._2 + "\n" }
    println(result.toString)
  }

  def apply(): Behaviors.Receive[ActorCommand] = Behaviors.receiveMessage[ActorCommand] { message =>
    message match {
      case Tweet(tweet) =>
        for (word <- tweet.split("\\s+")) {
          val numberPattern: Regex = "^(?i)[a-ząćęłńśóżź]".r

          numberPattern.findFirstMatchIn(word) match {
            case Some(_) =>
              val result: Int = wordMap getOrElse(word, 0)
              wordMap += (Filter.filterPunctuation(word.toLowerCase()) -> (result + 1))
            case None =>
          }
        }
      case SaveData() => save()
      case Stop() => Behaviors.stopped
      case Print() =>
        printResults()
        Behaviors.same
    }


    Behaviors.same
  }
}
