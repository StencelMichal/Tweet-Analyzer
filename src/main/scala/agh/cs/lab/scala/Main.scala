package agh.cs.lab.scala

import akka.actor.typed.ActorSystem
import Getter.Get
import SwearAnalyzer.LoadSwears
import agh.cs.lab.scala.Creator.Start

import scala.language.postfixOps


object Main extends App {


  val processors = List(WordCollector)
  implicit val creator: ActorSystem[ActorCommand] = ActorSystem(Creator(), "creator")

  creator ! Start(system = creator, interval = 10, amount = 10)

}