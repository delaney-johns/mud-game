package mud

import akka.actor.Actor
import akka.actor.ActorRef
import java.io.PrintStream
import java.io.BufferedReader
import java.net.ServerSocket

class Player(name: String, sock: ServerSocket, br: BufferedReader, ps: PrintStream) extends Actor {

  //Sets initial room location and inventory.
  private var inventory = new DLListBuffer[Item]()
  private var currentRoom: ActorRef = null

  import Player._
  def receive = {
    case Intro =>
      ps.println("Welcome to the game. Here are some helpful commands to get you started.")
      ps.println("north, south, east, west, up, down - moves your player.")
      ps.println("look - reprints the description of the current room")
      ps.println("inv - list the contents of your inventory")
      ps.println("get item - to get an item from the room and add it to your inventory")
      ps.println("drop item - to drop an item from your inventory into the room.")
      ps.println("exit - leave the game")
      ps.println("help - print the available commands and what they do.")
      ps.println("say message - tell everyone in your room something")
      ps.println("tell user message - tell anyone in the game something")

    case GetStartRoom(room) =>
      currentRoom = room
      currentRoom ! Room.PlayerEntersRoom(self)
      currentRoom ! Room.GetDescription
    case GetDescription(description) => ps.println(description)
    case ReceiveItem(itemOption: Option[Item]) =>
      if (itemOption != None) addToInventory(itemOption.get)
      else ps.println("That's not an item!")
    case TakeExit(room: Option[ActorRef]) =>
      if (room != None) {
        currentRoom ! Room.PlayerExitsRoom(self)
        currentRoom = room.get
        currentRoom ! Room.PlayerEntersRoom(self)
        currentRoom ! Room.GetDescription
      } else
        ps.println("You can't go that way!")
    case RequestStartRoom => Main.roomManager ! RoomManager.GetStartRoom
    case Print(message) => ps.println(message)
    case CheckInput =>
      if (br.ready()) {
        val input = br.readLine()
        if (input != null) {
          processCommand(input)
        }
      }
    case m => println("Unhandled message in player" + m)
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
      case "inv" => ps.println(inventoryListing())
      case s if s.startsWith("get") => currentRoom ! Room.GetItem(command.substring(4))
      case s if s.startsWith("drop") => addItemToRoom(dropItem(command.substring(5)))
      case "exit" => ps.println("Leave the game.")
      case "help" =>
        ps.println("north, south, east, west, up, down - moves your player.")
        ps.println("look - reprints the description of the current room")
        ps.println("inv - list the contents of your inventory")
        ps.println("get item - to get an item from the room and add it to your inventory")
        ps.println("drop item - to drop an item from your inventory into the room.")
        ps.println("exit - leave the game")
        ps.println("help - print the available commands and what they do.")
      case s if s.startsWith("say") => currentRoom ! Room.TellEveryoneInRoom(command.substring(4)) 
      case s if s.startsWith("tell") => 
        println("you typed tell.")
        Main.playerManager ! PlayerManager.TellOneUser(command.substring(command.indexOf(" ") + 1, command.lastIndexOf(" ")), command.substring(command.lastIndexOf(" ") + 1))
        println(command.substring(command.indexOf(" ") + 1, command.lastIndexOf(" ")))
        println(command.substring(command.lastIndexOf(" ") + 1))
      case _ => ps.println("Not a valid command.")

    }
  }

  //Finds an item out of the inventory (if the player has it) and returns the item.
  def getFromInventory(itemName: String): Option[Item] = {
    val indexOfItem = inventory.indexWhere(_.name == itemName)
    if (indexOfItem > -1) Some(inventory(indexOfItem)) else None
  }

  //Adds an item to a player's inventory.
  def addToInventory(item: Item): Unit = {
    inventory += item
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

  //Takes an item that the user typed and removes it
  //from the player's inventory.
  def dropItem(itemFromCommand: String): Option[Item] = {
    val indexOfItem = inventory.indexWhere(_.name == itemFromCommand)
    if (indexOfItem > -1) {
      val ret = Some(inventory(indexOfItem))
      inventory.remove(indexOfItem)
      //inventory = inventory.patch(indexOfItem, Nil, 1)
      ret
    } else None
  }

  //Takes an item and adds it to the current room.
  def addItemToRoom(item: Option[Item]): Unit = {
    if (item != None) {
      currentRoom ! Room.DropItem(item.get)
    } else ps.println("You don't even have that item!")
  }

  //Takes a direction that the player typed in and
  //moves the player to the room associated with that direction's exit.
  def move(dir: String): Unit = {
    val directionArray = Array("north", "south", "east", "west", "up", "down")
    val direction = directionArray.indexOf(dir)
    currentRoom ! Room.GetExit(direction)
  }

}

object Player {
  case object Intro
  case class GetStartRoom(room: ActorRef)
  case object CheckInput
  case class GetDescription(room: Room)
  case class ReceiveItem(itemOption: Option[Item])
  case class TakeExit(newRoom: Option[ActorRef])
  case object RequestStartRoom
  case class Print(message: String)
}