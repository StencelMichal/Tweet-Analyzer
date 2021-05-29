package agh.cs.lab.scala

import agh.cs.lab.scala.actorCommands.ActorCommand
import akka.actor.typed.ActorSystem
import agh.cs.lab.scala.actors.Getter.Get
import agh.cs.lab.scala.actors.SwearAnalyzer.LoadSwears
import agh.cs.lab.scala.actors.Creator.Start
import agh.cs.lab.scala.actors.{Creator, WordCollector}

import scala.language.postfixOps


object Main extends App {


  val processors = List(WordCollector)
  implicit val creator: ActorSystem[ActorCommand] = ActorSystem(Creator(), "creator")

  creator ! Start(system = creator, interval = 10, amount = 10)

}