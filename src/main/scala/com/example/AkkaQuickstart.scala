package com.example

import akka.http.scaladsl.model._
import HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{GenericHttpCredentials, OAuth2BearerToken}
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http

import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.example.Getter.Start
import com.example.AkkaQuickstart.getter

import scala.util.matching.Regex
import com.example.WordCollector.Tweet

import java.io.FileInputStream
import scala.language.postfixOps
import scala.util.parsing.json.JSON

object Getter {

  final case class Start(val system: ActorSystem[Getter.Start], interval: Int, amount: Int, processor: ActorSystem[WordCollector.Tweet])

  final val token: String = ???
  final val requestUri: String = "https://api.twitter.com/2/tweets/search/recent?query=-is%3Aretweet%20duda%20lang:pl&tweet.fields=author_id&max_results=10"

  def send_messages(system: ActorSystem[Getter.Start], processor1: ActorSystem[WordCollector.Tweet]): Unit = {
    val authorization = headers.Authorization(OAuth2BearerToken(token))
    val request = HttpRequest(GET, uri = requestUri, headers = Seq(authorization))
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val responseFuture: Future[HttpResponse] = Http(system).singleRequest(request)
    var result: List[String] = null
    print("3")

    responseFuture.onComplete {
      case Success(res) => {
        val messages: Future[String] = Unmarshal(res).to[String]
        println("2")
        messages.onComplete {
          case Success(res2) => {
            class CC[T] {
              def unapply(a: Any): Option[T] = Some(a.asInstanceOf[T])
            }
            print("1")

            object M extends CC[Map[String, Any]]
            object L extends CC[List[Any]]
            object D extends CC[String]

            result = for {
              Some(M(map)) <- List(JSON.parseFull(res2))
              L(data) = map("data")
              M(data_m) <- data
              D(text) = data_m("text")
            } yield {
              text
            }

            for (tweet <- result) {
              processor1 ! Tweet(tweet)
            }
          }
          case Failure(_) => sys.error("something wrong")
        }
      }
      case Failure(_) => sys.error("something wrong")
    }
  }

  def apply(): Behavior[Start] = Behaviors.setup { context =>

    //        val processor = context.spawn(WordCollector(), "processor")

    //        processor ! Tweet()

    Behaviors.receiveMessage { message =>
      println(message.toString)
      println("lol")
      send_messages(message.system, message.processor)
      Behaviors.same
    }

  }
}

//trait Processor {
//  final case class Tweet(tweet: String)
//}

object WordCollector {
  final case class Tweet(tweet: String)

  final case class SaveData(tweet: String)

  final val wordMap = collection.mutable.Map[String, Int]()

  def apply(): Behavior[Tweet] = Behaviors.receiveMessage { message =>
    println("dostałem")
    for (word <- message.tweet.split("\\s+")) {
      val numberPattern: Regex = "^(?i)[a-ząćęłńśóżź]".r

      numberPattern.findFirstMatchIn(word) match {
        case Some(_) =>
          //          println(word)
          val result: Int = wordMap getOrElse(word, 0)
          wordMap += (word -> (result + 1))
        case None =>
      }
    }
    //    println(message.tweet)
    print(wordMap.keys.size)
    wordMap.foreach { case (key: String, value: Int) => println(key + " " + value) }
    for ((word, count) <- wordMap) {
      println("1")
      println(word + " " + count)
    }
    Behaviors.same
  }
}

object SwearAnalyzer {
  sealed trait RoomCommand

  final case class Tweet(tweet: String) extends RoomCommand

  final case class LoadSwears(tweet: String) extends RoomCommand

  final case class SaveData(tweet: String) extends RoomCommand

  final val wordMap = collection.mutable.Map[String, Int]()

  def apply() = Behaviors.receiveMessage[RoomCommand] { message =>
    message match {
      case Tweet(tweet) =>
        println("dostałem")
        for (word <- tweet.split("\\s+")) {
          val numberPattern: Regex = "^(?i)[a-ząćęłńśóżź]".r

          numberPattern.findFirstMatchIn(word) match {
            case Some(_) =>
              //          println(word)
              val result: Int = wordMap getOrElse(word, 0)
              wordMap += (word -> (result + 1))
            case None =>
          }
        }
        //    println(message.tweet)
        print(wordMap.keys.size)
        wordMap.foreach { case (key: String, value: Int) => println(key + " " + value) }
        for ((word, count) <- wordMap) {
          println("1")
          println(word + " " + count)
        }
        Behaviors.same
      case LoadSwears(_) =>
        val stream = new FileInputStream("../../../resources/wulgaryzmy.json")
        val json = try {
          JSON.parseFull(stream.toString)
        } finally {
          stream.close()
        }
        Behaviors.same
    }

  }
}

//
////object
////final case class Tweet(tweet: String)
////class Processor2 extends Actor {
////  override def receive: Receive =
////}
//
////#main-class
object AkkaQuickstart extends App {

  val stream = new FileInputStream("src/main/resources/wulgaryzmy.json")
  //  val json = try {
  //    JSON.parseFull(stream.toString)
  //  } finally {
  //    stream.close()
  //  }

//  val result2 = scala.io.Source.fromInputStream(stream).mkString.parseJson
//  val resLust = result.toList
//  println(resLust)
  //  val jsonString = os.read("src/main/resources/wulgaryzmy.json")
  //  val data = ujson.read(jsonString)
  //  data("last_name") = "Poker Brat"
  //  os.write(os.pwd/"tmp"/"poker_brat.json", data)


  class CC[T] {
    def unapply(a: Any): Option[T] = Some(a.asInstanceOf[T])
  }

  object M extends CC[Map[String, Any]]

  object L extends CC[List[String]]

  object D extends CC[String]

//  println(scala.io.Source.fromInputStream(stream).mkString)

  val result = for {
    Some(M(map)) <- List(JSON.parseFull(scala.io.Source.fromInputStream(stream).mkString))
    L(wulgaryzmy) = map("wulgaryzmy")
    D(text) <- wulgaryzmy
  } yield {
    text
  }

  println(result.getClass.toString)

  for (swear <- result) {
    println(swear)
  }


  implicit val processor1: ActorSystem[WordCollector.Tweet] = ActorSystem(WordCollector(), "processor")
  //  implicit val processor2: ActorSystem[WordCollector.Tweet] = ActorSystem(WordCollector(), "processor")
  println("DUPA")

  val processors = List(WordCollector)
  implicit val getter: ActorSystem[Getter.Start] = ActorSystem(Getter(), "getter")
  getter ! Start(system = getter, interval = 10, amount = 10, processor1)


}