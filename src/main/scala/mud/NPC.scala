package mud

import akka.actor.Actor
import akka.actor.ActorRef
import scala.util.Random

class NPC(name: String) extends Actor {
  import Character._
  private var currentRoom: ActorRef = null
  val r = new Random()
  
  
  def receive = {
    case RequestStartRoom => Main.roomManager ! RoomManager.GetStartRoom
    case GetStartRoom(room) =>
      currentRoom = room
      currentRoom ! Room.PlayerEntersRoom(self)
      currentRoom ! Room.GetDescription
    case MoveNPC =>
      //finds a random room for NPC to go to and schedules NPC to move in the future
      val randomRoomChooser = r.nextDouble() * 4
      currentRoom ! Room.GetExit((randomRoomChooser).toInt)
      Main.activityManager ! ActivityManager.Enqueue(10, ScheduleMove)
    case ScheduleMove => Main.activityManager ! ActivityManager.Enqueue(10, MoveNPC)
    case _ =>
  }
}