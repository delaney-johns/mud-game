package mud
import scala.io.StdIn.readLine

import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

//Creates a player and processes user input.
object Main extends App {
  val system = ActorSystem("MUDSystem")
  val playerManager = system.actorOf(Props[PlayerManager], "PlayerManagerActor")
  val roomManager = system.actorOf(Props[RoomManager], "RoomManagerActor")
  val playerActor = system.actorOf(Props[Player], "PlayerActor")
//  playerActor ! PlayerManager.AddNewPlayer("player1")

  
  

//  val player = new Player
//  private var input = readLine
//  while (input != "exit") {
//    player.processCommand(input)
//    input = readLine
//  }
  system.scheduler.scheduleOnce(0 seconds, playerManager, PlayerManager.AddNewPlayer("player1"))

 system.scheduler.schedule(0.1.seconds, 0.1.seconds, playerManager, PlayerManager.Refresh)

}
