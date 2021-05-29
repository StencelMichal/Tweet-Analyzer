package agh.cs.lab.scala.actors

import agh.cs.lab.scala.actorCommands.{ActorCommand, SaveData, Stop}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object InputProcessor {


  final case class Start(actors: List[ActorRef[ActorCommand]])

  def processInput(actors: List[ActorRef[ActorCommand]]): Unit = {
    while (1 == 1) {
      val input = scala.io.StdIn.readLine()
      input.toLowerCase() match {
        case "save" =>
          for (actor <- actors) {
            actor ! SaveData()
          }
        case "stop" =>
          for (actor <- actors) {
            actor ! Stop()
          }
      }
    }
  }

  def apply(): Behavior[Start] = Behaviors.setup { context =>

    Behaviors.receiveMessage(message => {
      message match {
        case Start(actors) =>
          processInput(actors)
      }
      Behaviors.same
    })

  }
}
