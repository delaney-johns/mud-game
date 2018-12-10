package mud

import akka.actor.Actor
import akka.actor.ActorRef
import java.io.PrintStream
import java.io.BufferedReader
import java.net.ServerSocket
import scala.util.Random
import java.net.Socket

class Player(name: String, sock: Socket, br: BufferedReader, ps: PrintStream) extends Actor {

  //Sets initial room location and inventory.
  private var inventory = new DLListBuffer[Item]()
  private var currentRoom: ActorRef = null
  private var _equippedItem: Item = null
  //Default weapon
  def equippedItem = if (_equippedItem == null) Item("fists", "", 30, 40) else _equippedItem

  private var health = 100
  private var isDead = false
  private var victim: ActorRef = null

  //Used to randomize damage during combat
  val r = new Random()
  private def randomAddingOrSubtractingDamage = r.nextInt(equippedItem.damage / 2) + 1

  import Characters._
  def receive = {
    case Intro =>
      ps.println("\nWelcome to the game. Here are some helpful commands to get you started.")
      ps.println("north, south, east, west, up, down - moves your player.")
      ps.println("look - reprints the description of the current room")
      ps.println("inv - list the contents of your inventory")
      ps.println("get item - to get an item from the room and add it to your inventory")
      ps.println("drop item - to drop an item from your inventory into the room.")
      ps.println("equip item - equip an item for combat")
      ps.println("unequip item - unequip an item for combat")
      ps.println("exit - leave the game")
      ps.println("kill player - enter into combat with someone in your room.")
      ps.println("flee - run away if someone hits you. It might fail, so keep trying if you really want to live!")
      ps.println("help - print the available commands and what they do.")
      ps.println("say message - tell everyone in your room something")
      ps.println("tell user message - tell anyone in the game something\n")
    case CheckIfCharacterIsInRoomResponse(character) =>
      if (character != None) {
        victim = character.get
        Main.activityManager ! ActivityManager.Enqueue(equippedItem.speed, DoHit)
      } else ps.println("\nYou are waiving your weapon in the air. No characters to be found.")
    case GetStartRoom(room) =>
      currentRoom = room
      currentRoom ! Room.PlayerEntersRoom(self)
      currentRoom ! Room.GetDescription
    case GetDescription(description) => ps.println(description)
    case ReceiveItem(itemOption: Option[Item]) =>
      if (itemOption != None) addToInventory(itemOption.get)
      else ps.println("\nThat's not an item!")
    case TakeExit(room: Option[ActorRef]) =>
      if (room != None) {
        currentRoom ! Room.PlayerExitsRoom(self)
        currentRoom = room.get
        currentRoom ! Room.PlayerEntersRoom(self)
        currentRoom ! Room.GetDescription
      } else
        ps.println("\nYou can't go that way!")
    case RequestStartRoom => Main.roomManager ! RoomManager.GetStartRoom
    case Print(message) => ps.println(message)
    case DoHit =>
      if (health >= 0) victim ! IncomingHit(equippedItem.damage, currentRoom)
    //TODO: future fun enhancing update: make people miss their target during combat
    case IncomingHit(damage, room) =>
      if (currentRoom == room) {
        health -= randomAddingOrSubtractingDamage
        ps.println("\nYou have been hit by " + sender.path.name + ". Your health is " + health + ".")
        if (health > 0) {
          if (victim == null) {
            victim = sender
            Main.activityManager ! ActivityManager.Enqueue(equippedItem.speed, DoHit)
          }
        } else {
          victim = null
          ps.println("\nYou have died.")
          currentRoom ! Room.PlayerExitsRoom(self)
          sock.close()
          context.stop(self)
        }
        sender ! IncomingHitResponse(health <= 0, true)

      } else {
        sender ! IncomingHitResponse(false, false)
      }
    case IncomingHitResponse(victimIsDead, victimIsInRoom) =>
      if (!victimIsInRoom) {
        ps.println("You and your victim are no longer in the same room. Someone is a coward!")
        victim = null
      } else if (victimIsDead) {
        ps.println("\nYour victim has died.")
        victim = null
      } else {
        ps.println("\nYou hit " + victim.path.name + ".")
        Main.activityManager ! ActivityManager.Enqueue(equippedItem.speed, DoHit)
      }
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
      //If you are in combat, you cannot move normally and must type 'flee'.
      case "north" => if (victim == null) move("north")
      case "south" => if (victim == null) move("south")
      case "east" => if (victim == null) move("east")
      case "west" => if (victim == null) move("west")
      case "up" => if (victim == null) move("up")
      case "down" => if (victim == null) move("down")
      case "flee" => if (victim != null) currentRoom ! Room.GetExit((r.nextDouble() * 6).toInt) else {
        ps.println("\nWhy are you trying to flee? No one is attacking you!")
      }
      case "look" => currentRoom ! Room.GetDescription
      case "inv" => ps.println(inventoryListing())
      case s if s.startsWith("drop") => addItemToRoom(dropItem(command.substring(5)))
      case s if s.startsWith("equip") => equipItem(command.substring(6))
      case s if s.startsWith("get") => currentRoom ! Room.GetItem(command.substring(4))
      case s if s.startsWith("kill") => currentRoom ! Room.CheckIfCharacterIsInRoom(self, command.substring(5))
      case s if s.startsWith("shortestPath") => Main.roomManager ! RoomManager.FindShortestPath(currentRoom.path.name, command.substring(13))

      case s if s.startsWith("unequip") => unequipItem()
      case "exit" =>
        ps.println("\nThanks for playing!")
        sock.close()
        context.stop(self)
      case "help" =>
        ps.println("north, south, east, west, up, down - moves your player.")
        ps.println("look - reprints the description of the current room")
        ps.println("inv - list the contents of your inventory")
        ps.println("get item - to get an item from the room and add it to your inventory")
        ps.println("drop item - to drop an item from your inventory into the room.")
        ps.println("exit - leave the game")
        ps.println("help - print the available commands and what they do.\n")
      case s if s.startsWith("say") => currentRoom ! Room.TellEveryoneInRoom(command.substring(4))
      case s if s.startsWith("tell") =>
        //makes array of "tell", player's name, and message to send
        val playerNameAndMessageInput = command.split(" ", 3)
        Main.playerManager ! PlayerManager.TellOneUser(playerNameAndMessageInput(1), playerNameAndMessageInput(2))
      case _ => ps.println("\nNot a valid command.")

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
    if (inventory.isEmpty) "\nInventory:\nNone"
    else {
      var inventoryDesc = ""
      for (items <- inventory)
        inventoryDesc += items.name + " - " + items.desc + " It has damage of " + items.damage + " and speed of " + items.speed + "." + "\n"
      "\nInventory: \n" + inventoryDesc
    }

  }

  //Takes an item that the user typed and removes it
  //from the player's inventory.
  def dropItem(itemFromCommand: String): Option[Item] = {
    val indexOfItem = inventory.indexWhere(_.name == itemFromCommand)
    if (indexOfItem > -1) {
      val ret = Some(inventory(indexOfItem))
      inventory.remove(indexOfItem)
      ret
    } else None
  }

  //Takes an item and adds it to the current room.
  def addItemToRoom(item: Option[Item]): Unit = {
    if (item != None) {
      currentRoom ! Room.DropItem(item.get)
    } else ps.println("\nYou don't even have that item!")
  }

  //Takes a direction that the player typed in and
  //moves the player to the room associated with that direction's exit.
  def move(dir: String): Unit = {
    val directionArray = Array("north", "south", "east", "west", "up", "down")
    val direction = directionArray.indexOf(dir)
    currentRoom ! Room.GetExit(direction)
  }

  def equipItem(itemName: String): Unit = {
    if (inventory.exists(itemName => true)) {
      if (equippedItem.name == "fists") {
        val indexOfItem = inventory.indexWhere(_.name == itemName)
        _equippedItem = inventory(indexOfItem)
        inventory.remove(indexOfItem)
      } else {
        ps.println("\nYou must unequip your weapon to equip a new one.")
      }
    } else ps.println("\nYou can only equip items that are in your inventory.")
  }

  def unequipItem(): Unit = {
    inventory += _equippedItem
    _equippedItem = null
  }

}

