package agh.cs.lab.scala

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import spray.json._
import DefaultJsonProtocol._

import java.io.{File, PrintWriter}
import scala.util.matching.Regex
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.immutable.ListMap

object WordCollector {
  final case class Tweet(tweet: String) extends Message

  final case class SaveData() extends Operation

  final case class Stop() extends Operation

  final val wordMap = collection.mutable.Map[String, Int]()

//  final val printer = new PrintWriter(new File("/src/main/data/wordCollector_results.json"))


  def save(): Unit = {
    case class Obj(count: Map[String, Int])
    implicit val format: RootJsonFormat[Obj] = jsonFormat1(Obj)
//    val jsonStr = Obj(Map[String, Int](wordMap)).toJson
//    val jsonStr = Obj(wordMap.toMap).toJson
    val jsonStr = Obj(ListMap(wordMap.toSeq.sortBy(_._2)(Ordering[Int].reverse):_*)).toJson

//    println(jsonStr)
    ResultPrinter.print("src/main/data/word_collector/WordCollector_"+ new SimpleDateFormat("YYYYMMdd_HHmmss").format(new Date) +".json", jsonStr.toString())
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