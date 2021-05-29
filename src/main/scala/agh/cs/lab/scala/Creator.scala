package agh.cs.lab.scala

import agh.cs.lab.scala.Getter.Get
import agh.cs.lab.scala.SwearAnalyzer.LoadSwears
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors


object Creator {

  final case class Start(system: ActorSystem[Creator.Start], interval: Int, amount: Int)

  final val token: String = ???
  final val requestUri: String = "https://api.twitter.com/2/tweets/search/recent?tweet.fields=author_id,public_metrics&query=\"*\"%20lang:pl%20-is%3Aretweet&max_results=10"

  def apply(): Behavior[Start] = Behaviors.setup { context =>
    val getter = context.spawn(Getter("\"*\""), "getter")
    val wordCollector = context.spawn(WordCollector(), "wordCollector")
    val swearAnalyzer = context.spawn(SwearAnalyzer(), "swearAnalyzer")
    swearAnalyzer ! LoadSwears("src/main/resources/wulgaryzmy.json")
    Behaviors.receiveMessage(message => {
      getter ! Get(system = message.system, message.interval, message.amount, wordCollector, swearAnalyzer)
      Behaviors.same
    })


  }
}
