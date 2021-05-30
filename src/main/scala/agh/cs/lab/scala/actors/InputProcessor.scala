package agh.cs.lab.scala.actors

import agh.cs.lab.scala.actorCommands.{ActorCommand, Print, SaveData, Stop}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object InputProcessor {
  var running = true

  final case class Start(actors: List[ActorRef[ActorCommand]], system: ActorSystem[ActorCommand]) extends ActorCommand

  def processInput(actors: List[ActorRef[ActorCommand]], creator: ActorRef[ActorCommand]): Unit = {
    while (running) {
      val input = scala.io.StdIn.readLine()
      input.toLowerCase() match {
        case "save" =>
          for (actor <- actors) {
            actor ! SaveData()
          }
        case "stop" =>
          running = false
          creator ! Stop()
        case "print" =>
          for(actor <- actors){
            actor ! Print()
          }
      }
    }
  }

  def apply(): Behavior[ActorCommand] = Behaviors.setup { context =>

    Behaviors.receiveMessage {
      case Start(actors, creator) =>
        processInput(actors, creator)
        Behaviors.same
      case Stop() =>
        Behaviors.stopped
    }

  }
}
