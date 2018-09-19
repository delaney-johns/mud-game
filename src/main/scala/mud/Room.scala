package mud

import scala.io.Source

class Room(
  //Sets the name, description, exits, and items in each room.
  name: String,
  desc: String,
  exits: Array[Option[String]],
  private var items: List[Item]) {

  //Gets the room that is reached by going in a direction,
  //if there is an exit in that direction.
  def getExit(dir: Int): Option[Room] = {
    exits(dir).map(Room.rooms)
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
    name + "\n" + desc + "\n" + "Items: " + printList + "\n" + "Exits: " + printExits
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
  val rooms = readRooms()

  //Map of rooms is created using map.txt file.
  def readRooms(): Map[String, Room] = {
    val source = Source.fromFile("map.txt")
    val lines = source.getLines()
    val rooms = Array.fill(lines.next().trim().toInt)(readRoom(lines)).toMap
    source.close
    rooms
  }
 

  //File of the map is used to determine number of room,
  //name and description of room, possible exits, and items. 
  //Map is created with room name and info.
  def readRoom(lines: Iterator[String]): (String, Room) = {
    val number = lines.next()
    val name = lines.next()
    val desc = lines.next()
    val exits = lines.next().split(",").map(_.trim).map(i => if (i == "-1") None else Some(i))
    val items = List.fill(lines.next().trim.toInt) {
      val Array(name, desc) = lines.next().split(",", 2)
      Item(name.trim, desc.trim)

    }
    val r = new Room(name, desc, exits, items)
    (name, r)
  }

}