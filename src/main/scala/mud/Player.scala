package mud

class Player {
  private var inventory = List[Item]()
  private var currentRoom = Room.rooms(0)
  def processCommand(command: String): Unit = { command match {
    case "north" => move("north")
    case "south" => move("south")
    case "east" => move("east")
    case "west" => move("west")
    case "up" => move("up")
    case "down" => move("down")
    case "look" => println(currentRoom.description)
    case "inv" => println(inventoryListing())
    case s if s.startsWith("get") => findItem(command.substring(4))
    //substring for get item
    //case drop item => dropItem(item) item :: Room
    case "exit" => println("Leave the game.")
    case "help" => println("north, south, east, west, up, down - moves your player.")
      println("look - reprints the description of the current room")
      println("inv - list the contents of your inventory")
      println("get item - to get an item from the room and add it to your inventory")
      println("drop item - to drop an item from your inventory into the room.")
      println("exit - leave the game")
      println("help - print the available commands and what they do.")
    case _ => println("Not a valid command.")

    
  }
  //parse/act on command  
  }
  def getFromInventory(itemName: String): Option[Item] = {
    val indexOfItem = inventory.indexWhere(_.name == itemName) 
    if (indexOfItem > -1) Some(inventory(indexOfItem)) else None
  }
  def addToInventory(item: Item): Unit = {
    inventory ::= item 
  }
  def inventoryListing(): String = {
    if (inventory.isEmpty) "Items: None"
    else {
      val inventoryDesc = for (items <- inventory) 
        yield items.name + " - " + items.desc + "\n"
    
    "Inventory: " + inventoryDesc
    }
  }
  
  def findItem(itemFromCommand: String): Unit = {
    val gottenItem = currentRoom.getItem(itemFromCommand)
    if (gottenItem != None) addToInventory(gottenItem.getOrElse(new Item("","")))
  }
 
  
  def move(dir: String): Unit = { 
    val directionArray = Array("north","south", "east", "west", "up", "down")
    val direction = directionArray.indexOf(dir)
    if (currentRoom.getExit(direction) != None) {
      val newRoom = currentRoom.getExit(direction).getOrElse(new Room("", "", Array(), List()))
    currentRoom = newRoom
    }
    currentRoom.description
  }
}