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
  val playerActor = system.actorOf(Props[PlayerManager], "PlayerActor")
  playerActor ! PlayerManager.AddNewPlayer(playerActor)
  playerActor ! RoomManager.GetStartRoom
  playerActor ! Player.Intro
  
  

//  val player = new Player
//  private var input = readLine
//  while (input != "exit") {
//    player.processCommand(input)
//    input = readLine
//  }
 system.scheduler.schedule(0.seconds, 0.1.seconds, playerManager, PlayerManager.Refresh)

}
