package mud

import scala.io.Source

class Room (
  name: String,
  desc: String,
  exits: Array[Option[Int]],
  private var items: List[Item]) {

  def getExit(dir: Int): Option[Room] = {
    exits(dir).map(Room.rooms)
  }
  override def toString(): String = {
    name + "\n" + desc
  }

  def printList(): String = {
    var listString = ""
    for (index <- 0 until items.length) {
     // if (items.length == 0)
      listString += items(index).name + ", "
    }
    listString
  }
  
  //format better
  def printExits(): String = {
    var exitString = ""
    val exitDirections = Array("north", "south", "east", "west", "up", "down")
    for (index <- 0 until exits.length) {
      if (exits(index) != None) exitString += exitDirections(index) + " "
    }
    exitString
  }
  
  def description(): String = {
    name + "\n" + desc + "\n" + "Items: " + printList + "\n" + "Exits: " + printExits
  }

  def getItem(itemName: String): Option[Item] = {
    val indexOfItem = items.indexWhere(_.name == itemName)
    if (indexOfItem > -1) {
      val ret = Some(items(indexOfItem))
      items.patch(indexOfItem, Nil, 1)
      ret
    } else None
  }

  def dropItem(item: Item): Unit = {
  items = item :: items
  }

}

object Room {
  val rooms = readRooms()

  def readRooms(): Array[Room] = {
    val source = Source.fromFile("map.txt")
    val lines = source.getLines()
    val rooms = Array.fill(lines.next().trim().toInt)(readRoom(lines))
    source.close
    rooms
  }
  def readRoom(lines: Iterator[String]): Room = {
    val number = lines.next()
    val name = lines.next()
    val desc = lines.next()
    val exits = lines.next().split(",").map(_.trim.toInt).map(i => if (i == -1) None else Some(i))
    val items = List.fill(lines.next().trim.toInt) {
      val Array(name, desc) = lines.next().split(",", 2)
      Item(name.trim, desc.trim)

    }
    new Room(name, desc, exits, items)
  }
  

}