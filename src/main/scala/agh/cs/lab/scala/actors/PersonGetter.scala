package agh.cs.lab.scala.actors

import agh.cs.lab.scala.Main.creator
import agh.cs.lab.scala.actorCommands.{ActorCommand, Stop, TweetWithLikes}
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

object PersonGetter {
  var running = true;

  final case class Get(system: ActorSystem[Creator.Start], amount: Int, interval: Int, tweetAuthorId: Int,
                       likeAnalyzer: ActorRef[ActorCommand], token:String) extends ActorCommand

  def send_messages(requestUri: String, system: ActorSystem[Creator.Start],
                    likeAnalyzer: ActorRef[ActorCommand], interval: Int, token: String): Unit = {
    var modifiedRequest = requestUri
    val authorization = headers.Authorization(OAuth2BearerToken(token))

    while (running) {
      val request = HttpRequest(GET, uri = modifiedRequest, headers = Seq(authorization))
      implicit val executionContext: ExecutionContextExecutor = system.executionContext

      val responseFuture: Future[HttpResponse] = Http(system).singleRequest(request)

      responseFuture.onComplete {
        case Success(res) =>
          val messages: Future[String] = Unmarshal(res).to[String]
          messages.onComplete {
            case Success(res2) =>
              class CC[T] {
                def unapply(a: Any): Option[T] = Some(a.asInstanceOf[T])
              }

              object M extends CC[Map[String, Any]]
              object L extends CC[List[Any]]
              object D extends CC[String]
              object P extends CC[Map[String, Double]]

              val result = for (Some(M(map)) <- List(JSON.parseFull(res2)); L(data) = map("data"); M(data_m) <- data; D(text) = data_m("text"); P(metrics) = data_m("public_metrics"))
                yield (text: String, metrics("like_count").toInt)

              for ((tweet, likes) <- result) {
                likeAnalyzer ! TweetWithLikes(tweet, likes)
              }

              val token = for (Some(M(map)) <- List(JSON.parseFull(res2)); M(meta) = map("meta"))
                yield meta("next_token")

              modifiedRequest = requestUri + "&pagination_token=" + token.head
            case Failure(_) => sys.error("something wrong")
          }
        case Failure(_) => sys.error("something wrong")
      }

      Thread.sleep(interval * 1000)
    }

  }

  def apply(): Behavior[ActorCommand] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case Get(system, amount, interval, tweetAuthorId, likeAnalyzer, token) =>
        val requestUri = "https://api.twitter.com/2/users/" + tweetAuthorId.toString + "/tweets?tweet.fields=public_metrics&max_results=" + amount.toString
        val thread = new Thread {
          override def run() {
            send_messages(requestUri, system, likeAnalyzer, interval, token)
          }
        }
        thread.start()
        Behaviors.same
      case Stop() =>
        running = false
        Behaviors.stopped
    }
  }
}
