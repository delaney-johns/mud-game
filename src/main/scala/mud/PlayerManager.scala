package mud

import java.io.BufferedReader
import java.io.PrintStream

import akka.actor.Actor
import akka.actor.actorRef2Scala
import akka.actor.Props
import java.net.ServerSocket

class PlayerManager extends Actor {

  import PlayerManager._
  def receive = {
    case AddNewPlayer(player, sock, br, ps) =>
      val p = context.actorOf(Props(new Player(player, sock, br, ps)), player)
      println(p)
      p ! Player.RequestStartRoom
      p ! Player.Intro
    //player size one
    case Refresh => context.children.foreach(p => p ! Player.CheckInput)
    //player size zero again
    case _ =>
  }
}

object PlayerManager {
  case class AddNewPlayer(player: String, sock: ServerSocket, br: BufferedReader, ps: PrintStream)
  case object Refresh
}