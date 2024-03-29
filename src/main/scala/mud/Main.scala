package mud
import java.net.ServerSocket

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.actor.Props
import java.io.PrintStream
import java.io.BufferedReader
import java.io.InputStreamReader

//Creates a player and processes user input.
object Main extends App {
  val system = ActorSystem("MUDSystem")
  val playerManager = system.actorOf(Props[PlayerManager], "PlayerManagerActor")
  val roomManager = system.actorOf(Props[RoomManager], "RoomManagerActor")
  val activityManager = system.actorOf(Props[ActivityManager], "ActivityManagerActor")
  val npcManager = system.actorOf(Props[NPCManager], "NPCManagerActor")
  npcManager ! NPCManager.MakeNewNPC
  
  //val playerActor = system.actorOf(Props[Player], "PlayerActor")
  //  playerActor ! PlayerManager.AddNewPlayer("player1")
  system.scheduler.schedule(0.1.seconds, 0.1.seconds, playerManager, PlayerManager.Refresh)
  system.scheduler.schedule(0.1.seconds, 0.1.seconds, activityManager, ActivityManager.CheckInput)

  val ss = new ServerSocket(4040)
  while (true) {
    val sock = ss.accept()
    Future {
      val ps = new PrintStream(sock.getOutputStream)
      val br = new BufferedReader(new InputStreamReader(sock.getInputStream))
      ps.println("What is your name?")
      val name = br.readLine()
      playerManager ! PlayerManager.AddNewPlayer(name, sock, br, ps)
    }
  }

}
