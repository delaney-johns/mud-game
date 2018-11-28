package mud

import scala.io.Source

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class RoomManager extends Actor {
  import RoomManager._
  def receive = {
    case GetStartRoom =>
      sender ! Characters.GetStartRoom(rooms("Porch"))
    case FindShortestPath(currentRoomName, destinationRoom) =>
      sender ! Characters.Print(shortestPath(currentRoomName, destinationRoom, roomMap).toString())
    case _ =>
  }
  private val roomMap = collection.mutable.Map[String, Array[String]]()
  private val rooms = readRooms()

  for ((_, room) <- rooms) room ! Room.LinkExits(rooms)

  //Map of rooms is created using map.txt file.
  def readRooms(): BSTMap[String, ActorRef] = {
    val source = Source.fromFile("map.txt")
    val lines = source.getLines()
    val rooms = Array.fill(lines.next().trim().toInt)(readRoom(lines))
    source.close()
    val map = new BSTMap[String, ActorRef](_ < _)
    for (r <- rooms) map += r
    map
  }

  //File of the map is used to determine number of room,
  //name and description of room, possible exits, and items.
  //Map is created with room name and info.
  def readRoom(lines: Iterator[String]): (String, ActorRef) = {
    val keyword = lines.next()
    val name = lines.next()
    val desc = lines.next()
    val exits = lines.next().split(",").map(_.trim)
    val items = List.fill(lines.next().trim.toInt) {
      val Array(name, desc, damage, speed) = lines.next().split(",", 4)
      Item(name.trim, desc.trim, damage.trim.toInt, speed.trim.toInt)

    }
    roomMap(keyword) = exits
    keyword -> context.actorOf(Props(new Room(keyword, name, desc, exits, items)), keyword)

  }

  def shortestPath(node1: String, node2: String, connect: collection.mutable.Map[String, Array[String]],
    visited: Set[String] = Set.empty[String]): List[String] = {
    val exitDirections = Array("north", "south", "east", "west", "up", "down")
    if (node1 == node2) List(node2)
    else if (visited(node1)) Nil else {
      val newVisited = visited + node1
      val pathOptions = for ((nextRoom, direction) <- connect(node1).zip(exitDirections); if nextRoom != "-1") yield {
        direction :: shortestPath(nextRoom, node2, connect, newVisited)
      }
      val validPathOptions = pathOptions.filter(_.tail.nonEmpty)
      if (validPathOptions.isEmpty) Nil else node1 :: validPathOptions.minBy(_.length)
    }
  }
}

object RoomManager {
  case object GetStartRoom
  case class FindShortestPath(currentRoomName: String, roomDestination: String)
}