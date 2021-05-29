package agh.cs.lab.scala.actors

import agh.cs.lab.scala.Main.creator
import agh.cs.lab.scala.actorCommands.{ActorCommand, Tweet}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.parsing.json.JSON
import scala.util.{Failure, Success}

object Getter {

  final case class Get(system: ActorSystem[ActorCommand], interval: Int, amount: Int, actors: List[ActorRef[ActorCommand]]) extends ActorCommand

  final val token: String = ???
  final val requestUri: String = "https://api.twitter.com/2/tweets/search/recent?tweet.fields=author_id,public_metrics&query=\"*\"%20lang:pl%20-is%3Aretweet&max_results=100"

  def send_messages(system: ActorSystem[ActorCommand], actors: List[ActorRef[ActorCommand]]): Unit = {
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
              for (actor <- actors) {
                actor ! Tweet(tweet)
              }
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
      send_messages(message.system, message.actors)
      Behaviors.same
    }

  }
}
