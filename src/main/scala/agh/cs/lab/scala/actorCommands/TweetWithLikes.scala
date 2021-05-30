package agh.cs.lab.scala.actorCommands

final case class TweetWithLikes(tweet: String, likes: Int) extends ActorCommand
