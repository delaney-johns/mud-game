package mud
import scala.io.StdIn._

//Creates a player and processes user input.
object Main extends App {
  println("Welcome to the game. Here are some helpful commands to get you started.")
  println("north, south, east, west, up, down - moves your player.")
  println("look - reprints the description of the current room")
  println("inv - list the contents of your inventory")
  println("get item - to get an item from the room and add it to your inventory")
  println("drop item - to drop an item from your inventory into the room.")
  println("exit - leave the game")
  println("help - print the available commands and what they do.")

  val player = new Player
  private var input = readLine
  while (input != "exit") {
    player.processCommand(input)
    input = readLine
  }
}
