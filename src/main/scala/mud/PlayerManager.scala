package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class PlayerManager extends Actor {
  private var listOfAllPlayers = List[ActorRef]()
  def addPlayerToList(player: ActorRef) = {
    listOfAllPlayers = player :: listOfAllPlayers
  }
  import PlayerManager._
  def receive = {
    case AddNewPlayer(player) => val p = context.actorOf(Props(player), "p1")
p ! Player.GetStartRoom
p ! Player.Intro
    //player size one
    case Refresh => listOfAllPlayers.foreach(p => p ! Player.CheckInput)
    //player size zero again
    //case StartRoom =>
    case _ =>
  }
}

object PlayerManager {
  case class AddNewPlayer(player: ActorRef)
  case object Refresh
}