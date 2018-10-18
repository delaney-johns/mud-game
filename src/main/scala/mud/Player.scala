package mud

import scala.io.StdIn.readLine
import akka.actor.Actor
import akka.actor.ActorRef

class Player extends Actor {

  //Sets initial room location and inventory.
  private var inventory = List[Item]()
  //message- getstartroom room manager.getSR
  //sender ! player getSR (Rooms(room thing)
  private var currentRoom: ActorRef = null
  import Player._
  def receive = {
    case intro =>
      println("Welcome to the game. Here are some helpful commands to get you started.")
      println("north, south, east, west, up, down - moves your player.")
      println("look - reprints the description of the current room")
      println("inv - list the contents of your inventory")
      println("get item - to get an item from the room and add it to your inventory")
      println("drop item - to drop an item from your inventory into the room.")
      println("exit - leave the game")
      println("help - print the available commands and what they do.")
    case GetStartRoom(room) => currentRoom = room
    case GetDescription(room) => sender ! 
    case CheckInput =>
      val input = readLine()
      if (input != null) {
        processCommand(input)
      }
  }

  //Processes the user's command and takes the appropriate action.
  def processCommand(command: String): Unit = {
    command match {
      case "north" => move("north")
      case "south" => move("south")
      case "east" => move("east")
      case "west" => move("west")
      case "up" => move("up")
      case "down" => move("down")
      case "look" => currentRoom ! Room.GetDescription
      case "inv" => println(inventoryListing())
      case s if s.startsWith("get") => findItem(command.substring(4))
      case s if s.startsWith("drop") => addItemToRoom(dropItem(command.substring(5)))
      case "exit" => println("Leave the game.")
      case "help" =>
        println("north, south, east, west, up, down - moves your player.")
        println("look - reprints the description of the current room")
        println("inv - list the contents of your inventory")
        println("get item - to get an item from the room and add it to your inventory")
        println("drop item - to drop an item from your inventory into the room.")
        println("exit - leave the game")
        println("help - print the available commands and what they do.")
      case _ => println("Not a valid command.")

    }
  }

  //Finds an item out of the inventory (if the player has it) and returns the item.
  def getFromInventory(itemName: String): Option[Item] = {
    val indexOfItem = inventory.indexWhere(_.name == itemName)
    if (indexOfItem > -1) Some(inventory(indexOfItem)) else None
  }

  //Adds an item to a player's inventory.
  def addToInventory(item: Item): Unit = {
    inventory ::= item
  }

  //Shows items in an inventory, if any.
  def inventoryListing(): String = {
    if (inventory.isEmpty) "Inventory:\nNone"
    else {
      var inventoryDesc = ""
      for (items <- inventory)
        inventoryDesc += items.name + " - " + items.desc + "\n"
      "Inventory: \n" + inventoryDesc
    }
  }

  //Takes an item that the user typed and adds it to the player's
  //inventory, if the item is in the current room.
  def findItem(itemFromCommand: String): Unit = {
    val gottenItem = currentRoom.getItem(itemFromCommand)
    if (gottenItem != None) addToInventory(gottenItem.get)
  }

  //Takes an item that the user typed and removes it
  //from the player's inventory.
  def dropItem(itemFromCommand: String): Option[Item] = {
    val indexOfItem = inventory.indexWhere(_.name == itemFromCommand)
    if (indexOfItem > -1) {
      val ret = Some(inventory(indexOfItem))
      inventory = inventory.patch(indexOfItem, Nil, 1)
      ret
    } else None
  }

  //Takes an item and adds it to the current room.
  def addItemToRoom(item: Option[Item]): Unit = {
    if (item != None) currentRoom.dropItem(item.get)
  }

  //Takes a direction that the player typed in and
  //moves the player to the room associated with that direction's exit.
  def move(dir: String): Unit = {
    val directionArray = Array("north", "south", "east", "west", "up", "down")
    val direction = directionArray.indexOf(dir)
    if (currentRoom.getExit(direction) != None) {
      val newRoom = currentRoom.getExit(direction).get
      currentRoom = newRoom
    }
    println(currentRoom.description)
  }

}

object Player {
  case object Intro
  case class GetStartRoom(room: ActorRef)
  case object CheckInput
  case class Print(string: String)
  case class GetDescription(room: ActorRef)
}