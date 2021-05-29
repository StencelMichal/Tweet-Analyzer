package agh.cs.lab.scala


import agh.cs.lab.scala.Main.creator
import agh.cs.lab.scala.SwearAnalyzer.Tweet
//import agh.cs.lab.scala.WordCollector.Tweet
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, headers}
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import scala.util.parsing.json.JSON

object Getter {

  final case class Get(system: ActorSystem[Creator.Start], interval: Int, amount: Int,
                       wordCollector: ActorRef[WordCollector.Tweet], swearAnalyzer: ActorRef[SwearAnalyzer.RoomCommand])

  final val token: String = "AAAAAAAAAAAAAAAAAAAAALN1PQEAAAAAiuy7kHX%2Fz%2FDq7nsVIAdt4BXKdDk%3Dfc9ON3GUgq8YPEvm4HjESYRtfMuc0TCQOqN6IEfmj0glgC4QT9"
  final val requestUri: String = "https://api.twitter.com/2/tweets/search/recent?tweet.fields=author_id,public_metrics&query=\"*\"%20lang:pl%20-is%3Aretweet&max_results=100"

  def send_messages(system: ActorSystem[Creator.Start], wordCollector: ActorRef[WordCollector.Tweet],
                    swearAnalyzer: ActorRef[Tweet]): Unit = {
    val authorization = headers.Authorization(OAuth2BearerToken(token))
    val request = HttpRequest(GET, uri = requestUri, headers = Seq(authorization))
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val responseFuture: Future[HttpResponse] = Http(system).singleRequest(request)
    var result: List[String] = null

    responseFuture.onComplete {
      case Success(res) => {
        val messages: Future[String] = Unmarshal(res).to[String]
        messages.onComplete {
          case Success(res2) => {
            class CC[T] {
              def unapply(a: Any): Option[T] = Some(a.asInstanceOf[T])
            }

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
              wordCollector ! WordCollector.Tweet(tweet)
              swearAnalyzer ! SwearAnalyzer.Tweet(tweet)
            }
          }
          case Failure(_) => sys.error("something wrong")
        }
      }
      case Failure(_) => sys.error("something wrong")
    }
  }

  def apply(query: String): Behavior[Get] = Behaviors.setup { context =>
    Behaviors.receiveMessage { message =>
      println(query)
      send_messages(message.system, message.wordCollector, message.swearAnalyzer)
      Behaviors.same
    }

  }
}
