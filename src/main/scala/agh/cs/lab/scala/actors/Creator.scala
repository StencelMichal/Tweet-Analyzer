package agh.cs.lab.scala.actors

import agh.cs.lab.scala.actorCommands.{ActorCommand, SaveData, Stop}
import agh.cs.lab.scala.actors.SwearAnalyzer.LoadSwears
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

object Creator {

  final case class Start(system: ActorSystem[ActorCommand], interval: Int, amount: Int) extends ActorCommand

  final val token: String = ???

  def apply(): Behavior[ActorCommand] = Behaviors.setup { context =>

    val getter = context.spawn(Getter(), "getter")
    val wordCollector = context.spawn(WordCollector(), "wordCollector")
    val swearAnalyzer = context.spawn(SwearAnalyzer(), "swearAnalyzer")

    swearAnalyzer ! LoadSwears("src/main/resources/wulgaryzmy.json")

    val personGetter = context.spawn(PersonGetter(), name = "personGetter")
    val likeAnalyzer = context.spawn(LikeAnalyzer(), name = "likeAnalyzer")

    val inputProcessor = context.spawn(InputProcessor(), name = "inputProcessor")

    Behaviors.receiveMessage {
      case Start(system, interval, amount) =>
        inputProcessor ! InputProcessor.Start(List(likeAnalyzer, swearAnalyzer, wordCollector), system)
        getter ! Getter.Get(system = system, interval, amount, List(wordCollector, swearAnalyzer), token)
        personGetter ! PersonGetter.Get(system = system, interval, amount, 202086424, likeAnalyzer, token)
        Behaviors.same
      case Stop() =>
        for (actor <- List(wordCollector, swearAnalyzer, likeAnalyzer)) {
          actor ! SaveData()
        }
        for (actor <- List(getter, personGetter)) {
          actor ! Stop()
        }
        for (actor <- List(wordCollector, swearAnalyzer, likeAnalyzer, inputProcessor)) {
          context.stop(actor)
        }
        Behaviors.stopped
    }

  }
}
