package agh.cs.lab.scala

import agh.cs.lab.scala.Getter.Get
import agh.cs.lab.scala.SwearAnalyzer.LoadSwears
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors


object Creator {

  final case class Start(system: ActorSystem[ActorCommand], interval: Int, amount: Int) extends ActorCommand

  final val token: String = "AAAAAAAAAAAAAAAAAAAAALN1PQEAAAAAiuy7kHX%2Fz%2FDq7nsVIAdt4BXKdDk%3Dfc9ON3GUgq8YPEvm4HjESYRtfMuc0TCQOqN6IEfmj0glgC4QT9"
  final val requestUri: String = "https://api.twitter.com/2/tweets/search/recent?tweet.fields=author_id,public_metrics&query=\"*\"%20lang:pl%20-is%3Aretweet&max_results=10"

  def apply(): Behavior[ActorCommand] = Behaviors.setup { context =>

    val getter = context.spawn(Getter("\"*\""), "getter")
    val wordCollector = context.spawn(WordCollector(), "wordCollector")
    val swearAnalyzer = context.spawn(SwearAnalyzer(), "swearAnalyzer")

    swearAnalyzer ! LoadSwears("src/main/resources/wulgaryzmy.json")

    val personGetter = context.spawn(PersonGetter("DUDA"), name = "personGetter")
    val likeAnalyzer = context.spawn(LikeAnalyzer(), name = "likeAnalyzer")

    val inputProcessor = context.spawn(InputProcessor(), name = "inputProcessor")

    Behaviors.receiveMessage {
      case Start(system, interval, amount) =>
        inputProcessor ! InputProcessor.Start(likeAnalyzer, swearAnalyzer, wordCollector)
        getter ! Get(system = system, interval, amount, wordCollector, swearAnalyzer)
        personGetter ! PersonGetter.Get(system = system, interval, amount, 202086424, likeAnalyzer)
        Behaviors.same
    }

  }
}
