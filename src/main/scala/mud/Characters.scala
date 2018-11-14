package mud

import akka.actor.ActorRef

//Messages received by both players and NPCs
object Characters {
  case object Intro
  case object CheckInput
  case class CheckIfCharacterIsInRoomResponse(character: Option[ActorRef])
  case object DoHit
  case class GetDescription(room: Room)
  case class GetStartRoom(room: ActorRef)
  case class HitAttempt(killer: ActorRef, victim: String)
  case class IncomingHit(damage: Int, currentRoom: ActorRef)
  case class IncomingHitResponse(victimIsDead: Boolean, victimIsInRoom: Boolean)
  case object MoveNPC
  case class Print(message: String)
  case class ReceiveItem(itemOption: Option[Item])
  case object RequestStartRoom
  case class TakeExit(newRoom: Option[ActorRef])
}