//#full-example
package com.example

import akka.http.scaladsl.model._
import HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.GenericHttpCredentials
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.example.GreeterMain.Setup

import akka.http.scaladsl.client.RequestBuilding.Get

object PrintMyActorRefActor {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new PrintMyActorRefActor(context))
}


class Getter(context: ActorContext[String]) extends AbstractBehavior[String](context){


  override def onMessage(msg: String): Behavior[String] = {
    get_messages(1,1)
  }   

  def get_messages(interval:Int, amount:Int) = {
    // while(true){
      // val token = 
      // val validCredentials = BasicHttpCredentials("John", "p4ssw0rd")
      val authorization = headers.Authorization(GenericHttpCredentials(token=token))
      // val request = new Get(uri = "https://api.twitter.com/2/tweets/search/recent?query=duda lang:pl&tweet.fields=author_id&max_results=10")
      val request = HttpRequest(GET, uri = "https://api.twitter.com/2/tweets/search/recent?query=duda lang:pl&tweet.fields=author_id&max_results=10",
                                    headers = Seq(authorization))
      println(request)
      // request.add

    // }
  }
  



  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Hello {}!", message.whom)
    //#greeter-send-messages
    message.replyTo ! Greeted(message.whom, context.self)
    //#greeter-send-messages
    Behaviors.same
  }
}

// //#greeter-actor
// object Greeter {
//   final case class Greet(whom: String, replyTo: ActorRef[Greeted])
//   final case class Greeted(whom: String, from: ActorRef[Greet])

//   def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
//     context.log.info("Hello {}!", message.whom)
//     //#greeter-send-messages
//     message.replyTo ! Greeted(message.whom, context.self)
//     //#greeter-send-messages
//     Behaviors.same
//   }
// }
//#greeter-actor

//#greeter-bot
// object GreeterBot {

//   def apply(max: Int): Behavior[Greeter.Greeted] = {
//     bot(0, max)
//   }

//   private def bot(greetingCounter: Int, max: Int): Behavior[Greeter.Greeted] =
//     Behaviors.receive { (context, message) =>
//       val n = greetingCounter + 1
//       context.log.info("Greeting {} for {}", n, message.whom)
//       if (n == max) {
//         Behaviors.stopped
//       } else {
//         message.from ! Greeter.Greet(message.whom, context.self)
//         bot(n, max)
//       }
//     }
// }
//#greeter-bot

object GreeterMain {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new Main(context))

}

//#greeter-main
class GreeterMain(context: ActorContext[String]) extends AbstractBehavior[String](context) {

  final case class Setup(name: String)

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "dupa" =>
        val getter = context.spawn(Getter(), "dupa2")
        // println(s"First: $firstRef")
        getter ! "dupa3"
        this
    }


  // def apply(): Behavior[Setup] =
  //   Behaviors.setup { context =>
  //     //#create-actors
  //     val getter = context.spawn(Getter(), "getter")
  //     getter ! "dupa"

  //     //#create-actors
  //     // Behaviors.receiveMessage { message =>
  //       //#create-actors
  //       // val replyTo = context.spawn(GreeterBot(max = 3), message.name)
  //       //#create-actors

  //       // getter ! "dupa"
  //       // Behaviors.same
  //     // }
  //   }
}
//#greeter-main

//#main-class
object AkkaQuickstart extends App {
  //#actor-system
  val main = ActorSystem(GreeterMain(), "AkkaQuickStart")
  //#actor-system+

  //#main-send-messages
  main ! Setup("Charles")
  //#main-send-messages
}
//#main-class
//#full-example
