package agh.cs.lab.scala.actors

import agh.cs.lab.scala.utils.{Filter, Latinifier, ResultPrinter}
import agh.cs.lab.scala.actorCommands.{ActorCommand, Message, Operation, SaveData, Tweet}
import akka.actor.typed.scaladsl.Behaviors
import spray.json.DefaultJsonProtocol.{DoubleJsonFormat, StringJsonFormat, jsonFormat1, mapFormat}
import spray.json.{RootJsonFormat, enrichAny}

import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import scala.util.parsing.json.JSON

object SwearAnalyzer {

  final case class LoadSwears(message: String) extends Message

  final val swearSet = collection.mutable.Set[String]()
  var tweets: Int = 0
  var vulgarTweets: Int = 0

  def save(): Unit = {
    case class Obj(count: Map[String, Double])
    implicit val format: RootJsonFormat[Obj] = jsonFormat1(Obj)

    val jsonStr = Obj(Map("vulgarTweets" -> vulgarTweets, "tweets" -> tweets, "vulgarRatio" -> 1.0 * tweets / vulgarTweets)).toJson

    ResultPrinter.print("src/main/data/swear_analyzer/SwearAnalyzer_" + new SimpleDateFormat("YYYYMMdd_HHmmss").format(new Date) + ".json", jsonStr.toString())
  }

  def apply(): Behaviors.Receive[ActorCommand] = Behaviors.receiveMessage[ActorCommand] {
    case Tweet(tweet) =>
      tweets += 1
      if (tweet.split("\\s+")
        .map(word => Filter.filterPunctuation(word))
        .map(word => Latinifier.latinify(word))
        .count(word => swearSet.contains(word)) > 0) {
        vulgarTweets += 1
        println(tweet)
      }
      println("vulgar", vulgarTweets)
      println("Ratio: " + 1.0 * vulgarTweets / tweets * 100 + "%")

      Behaviors.same
    case LoadSwears(path) =>
      val stream = new FileInputStream(path)

      class CC[T] {
        def unapply(a: Any): Option[T] = Some(a.asInstanceOf[T])
      }

      object M extends CC[Map[String, Any]]
      object L extends CC[List[String]]
      object D extends CC[String]

      val result = for {
        Some(M(map)) <- List(JSON.parseFull(scala.io.Source.fromInputStream(stream).mkString))
        L(swears) = map("wulgaryzmy")
        D(text) <- swears
      } yield {
        text
      }

      for (swear <- result) {
        swearSet.addOne(swear)
      }

      Behaviors.same
    case SaveData() =>
      save()
      Behaviors.same
  }
}
