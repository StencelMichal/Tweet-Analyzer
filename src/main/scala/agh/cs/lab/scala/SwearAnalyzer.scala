package agh.cs.lab.scala

import akka.actor.typed.scaladsl.Behaviors

import java.io.FileInputStream
import scala.util.parsing.json.JSON


object SwearAnalyzer {
  sealed trait RoomCommand

  final case class Tweet(tweet: String) extends RoomCommand

  final case class LoadSwears(path: String) extends RoomCommand

  final case class SaveData() extends RoomCommand

  final val swearSet = collection.mutable.Set[String]()
  var tweets: Int = 0
  var vulgarTweets: Int = 0

  def apply() = Behaviors.receiveMessage[RoomCommand] { message =>
    message match {
      case Tweet(tweet) =>
        tweets += 1
        if (tweet.split("\\s+")
          .map(word => word.toLowerCase.dropWhile(c => ",.\";\'".indexOf(c) > 0))
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
          L(wulgaryzmy) = map("wulgaryzmy")
          D(text) <- wulgaryzmy
        } yield {
          text
        }

        for (swear <- result) {
          swearSet.addOne(swear)
        }

        //        // printing
        //        for (swear <- swears) {
        //          println(swear)
        //        }
        Behaviors.same
    }

  }
}
