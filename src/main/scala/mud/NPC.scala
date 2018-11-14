package mud

import akka.actor.Actor
import akka.actor.ActorRef
import scala.util.Random

class NPC(name: String, health: Int) extends Actor {
  import Characters._
  private var currentRoom: ActorRef = null
  private var health = 80
  val r = new Random()

  
  Main.activityManager ! ActivityManager.Enqueue(300, MoveNPC)
  def receive = {
     case TakeExit(room: Option[ActorRef]) =>
      if (room != None) {
        currentRoom ! Room.PlayerExitsRoom(self)
        currentRoom = room.get
        currentRoom ! Room.PlayerEntersRoom(self)
      } 
    case RequestStartRoom => Main.roomManager ! RoomManager.GetStartRoom
    case GetStartRoom(room) =>
      currentRoom = room
      currentRoom ! Room.PlayerEntersRoom(self)
      currentRoom ! Room.GetDescription
    case MoveNPC =>
      //finds a random room for NPC to go to and schedules NPC to move in the future
      val randomRoomChooser = r.nextDouble() * 4
      currentRoom ! Room.GetExit((randomRoomChooser).toInt)
      Main.activityManager ! ActivityManager.Enqueue(300, MoveNPC)
    case Print(_) => 
    case m => println("unhandled message in NPC" + m)
   
  }
}