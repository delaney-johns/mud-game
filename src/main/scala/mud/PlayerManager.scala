package mud

import akka.actor.Actor
import akka.actor.ActorRef

class PlayerManager extends Actor {
  private var listOfAllPlayers = List[ActorRef]()
  def addPlayerToList(player:ActorRef) = {
    player :: listOfAllPlayers
  }
  import PlayerManager._
  def receive = {
    case AddNewPlayer(player) => addPlayerToList(player)
    case Refresh =>  listOfAllPlayers.foreach(p => p ! Player.CheckInput)
 
    case _ => 
  }
}

object PlayerManager {
  case class AddNewPlayer(player: ActorRef)
  case object Refresh
}