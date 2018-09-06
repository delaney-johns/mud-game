package mud
import scala.io.StdIn._


/**
This is a stub for the main class for your MUD.
*/
object Main extends App {
  println("Welcome to my MUD.")
	
	val player = new Player 
	private var input = readLine
	while (input != "exit") {
	  player.processCommand(input)
	  input = readLine
	}
}
