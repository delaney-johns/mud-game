package mud

import scala.io.Source

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class RoomManager extends Actor {
  import RoomManager._
  def receive = {
    case GetStartRoom =>
      sender ! Character.GetStartRoom(rooms("Porch"))
    case _ =>
  }

  private val rooms = readRooms()
  for ((_, room) <- rooms) room ! Room.LinkExits(rooms)

  //Map of rooms is created using map.txt file.
  def readRooms(): Map[String, ActorRef] = {
    val source = Source.fromFile("map.txt")
    val lines = source.getLines()
    val rooms = Array.fill(lines.next().trim().toInt)(readRoom(lines))
    source.close()
    rooms.toMap
  }

  //File of the map is used to determine number of room,
  //name and description of room, possible exits, and items.
  //Map is created with room name and info.
  def readRoom(lines: Iterator[String]): (String, ActorRef) = {
    val keyword = lines.next()
    val name = lines.next()
    val desc = lines.next()
    val exits = lines.next().split(",")
    val items = List.fill(lines.next().trim.toInt) {
      val Array(name, desc) = lines.next().split(",", 2)
      Item(name.trim, desc.trim)

    }
    keyword -> context.actorOf(Props(new Room(keyword, name, desc, exits, items)), keyword)

  }
}

object RoomManager {
  case object GetStartRoom
}