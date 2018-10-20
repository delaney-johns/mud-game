package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

class PlayerManager extends Actor {
  
  import PlayerManager._
  def receive = {
    case AddNewPlayer(player) => val p = context.actorOf(Props(new Player), player)
    println(p)
p ! Player.RequestStartRoom
p ! Player.Intro
    //player size one
    case Refresh => context.children.foreach(p => p ! Player.CheckInput)
    //player size zero again
    //case StartRoom =>
    case _ =>
  }
}

object PlayerManager {
  case class AddNewPlayer(player: String)
  case object Refresh
}