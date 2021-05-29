package agh.cs.lab.scala

import agh.cs.lab.scala.LikeAnalyzer.{SaveData, wordMap, Stop}
import agh.cs.lab.scala.SwearAnalyzer.LoadSwears
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object InputProcessor {


  final case class Start(likeAnalyzer: ActorRef[ActorCommand], swearAnalyzer: ActorRef[ActorCommand], wordCollector: ActorRef[ActorCommand])

  def processInput(likeAnalyzer: ActorRef[ActorCommand], swearAnalyzer: ActorRef[ActorCommand], wordCollector: ActorRef[ActorCommand]): Unit = {
    while (1 == 1) {
      val input = scala.io.StdIn.readLine()
      input.toLowerCase() match {
        case "save" =>
          likeAnalyzer ! LikeAnalyzer.SaveData()
          swearAnalyzer ! SwearAnalyzer.SaveData()
          wordCollector ! WordCollector.SaveData()
        case "stop" =>
          likeAnalyzer ! LikeAnalyzer.Stop()
          swearAnalyzer ! SwearAnalyzer.Stop()
          wordCollector ! WordCollector.Stop()
      }
    }
  }

  def apply(): Behavior[Start] = Behaviors.setup { context =>

    Behaviors.receiveMessage(message => {
      message match {
        case Start(likeAnalyzer, swearAnalyzer, wordCollector) =>
          processInput(likeAnalyzer, swearAnalyzer, wordCollector)
      }
      Behaviors.same
    })

  }
}