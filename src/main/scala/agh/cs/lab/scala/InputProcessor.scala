package agh.cs.lab.scala

import agh.cs.lab.scala.LikeAnalyzer.{SaveData, wordMap}
import agh.cs.lab.scala.SwearAnalyzer.LoadSwears
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object InputProcessor {


  final case class Start(system: ActorSystem[Creator.Start], likeAnalyzer: ActorRef[LikeAnalyzer.Tweet], swearAnalyzer: ActorRef[SwearAnalyzer.Tweet], wordCollector: ActorRef[WordCollector.Tweet], interval: Int, amount: Int)

  def processInput(likeAnalyzer: ActorRef[LikeAnalyzer.SaveData], swearAnalyzer: ActorRef[SwearAnalyzer.SaveData], wordCollector: ActorRef[WordCollector.SaveData]): Unit = {
    while (1 == 1) {
      val input = scala.io.StdIn.readLine()
      input.toLowerCase() match {
        case "save" => {
          likeAnalyzer ! SaveData
          swearAnalyzer ! SaveData
          wordCollector ! SaveData
        }

      }
    }
  }

  def apply(): Behavior[Start] = Behaviors.setup { context =>
    val getter = context.spawn(Getter("\"*\""), "getter")
    val wordCollector = context.spawn(WordCollector(), "wordCollector")
    val swearAnalyzer = context.spawn(SwearAnalyzer(), "swearAnalyzer")

    swearAnalyzer ! LoadSwears("src/main/resources/wulgaryzmy.json")

    val personGetter = context.spawn(PersonGetter("DUDA"), name = "personGetter")
    val likeAnalyzer = context.spawn(LikeAnalyzer(), name = "likeAnalyzer")

    Behaviors.receiveMessage(message => {
      //      getter ! Get(system = message.system, message.interval, message.amount, wordCollector, swearAnalyzer)
      personGetter ! PersonGetter.Get(system = message.system, message.interval, message.amount, 202086424, likeAnalyzer)
      Behaviors.same
    })

  }
}