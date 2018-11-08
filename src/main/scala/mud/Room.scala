package mud

import scala.io.Source
import akka.actor.Actor
import akka.actor.ActorRef

class Room(
  //Sets the name, description, exits, and items in each room.
  keyword: String,
  name: String,
  desc: String,
  exitKeys: Array[String],
  private var items: List[Item]) extends Actor {
  private var exits: Array[Option[ActorRef]] = null
  var playerList = List[ActorRef]()

  import Room._
  def receive = {
    case GetExit(dir) =>
      sender ! Character.TakeExit(getExit(dir))
      playerList.filter(_ != sender)
    case LinkExits(rooms) =>
      exits = exitKeys.map(key => rooms.get(key))
    case GetDescription =>
      sender ! Character.Print(description())
    case DropItem(item) => dropItem(item)
    case GetItem(itemName) => sender ! Character.ReceiveItem(getItem(itemName))
    case PlayerEntersRoom(player) => 
      playerList.foreach(_ ! Character.Print(player.path.name + " has arrived!"))
      playerList ::= player
    case PlayerExitsRoom(player) => playerList = playerList.filter(_ != player)
     playerList.foreach(_ ! Character.Print(player.path.name + " has left!"))
    case TellEveryoneInRoom(message) => 
      playerList.filter(_ != sender).foreach(_ ! Character.Print(sender.path.name + " said " + message))
    case _ =>
  }

  //Gets the room that is reached by going in a direction,
  //if there is an exit in that direction.
  def getExit(dir: Int): Option[ActorRef] = {
    exits(dir)
  }

  //Creates a string of the items that are in the current room.
  def printList(): String = {
    var listString = ""
    for (index <- 0 until items.length) {
      if (items.length == 1)
        listString += items(index).name
      else
        listString += items(index).name + ", "
    }
    listString
  }

  //Creates a string of the possible exits for the current room.
  def printExits(): String = {
    var exitString = ""
    val exitDirections = Array("north", "south", "east", "west", "up", "down")
    for (index <- 0 until exits.length) {
      if (exits(index) != None) {
        exitString += exitDirections(index)
        exitString += ", "
      }
    }
    exitString.substring(0, exitString.length - 2)
  }

  //Creates a string with the name, description, items, and exits for the current room.
  def description(): String = {
    name + "\n" + desc + "\n" + "Items: " + printList + "\n" + "Exits: " + printExits + "\n" + "Players here: " + playerList.map(_.path.name).mkString(", ") + "\n"
  }

  //Pulls an item from a room (if it is in the room) and returns it.
  //Item is removed from the room.
  def getItem(itemName: String): Option[Item] = {
    val indexOfItem = items.indexWhere(_.name == itemName)
    if (indexOfItem > -1) {
      val ret = Some(items(indexOfItem))
      items = items.patch(indexOfItem, Nil, 1)
      ret
    } else None
  }

  //Item is added to the room.
  def dropItem(item: Item): Unit = {
    items = item :: items
  }
}

object Room {
  //Messages sent by player
  case class GetExit(dir: Int)
  case class GetItem(itemName: String)
  case class DropItem(item: Item)
  case object GetDescription
  case class PlayerEntersRoom(player: ActorRef)
  case class PlayerExitsRoom(player: ActorRef)
  case class TellEveryoneInRoom(message: String)


  //Messages sent by RoomManager
  case class LinkExits(rooms: Map[String, ActorRef])

}