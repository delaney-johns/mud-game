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
      p ! Character.RequestStartRoom
      p ! Character.Intro
    case Refresh => context.children.foreach(p => p ! Character.CheckInput)
    case TellOneUser(playerName, message) =>
      context.children.filter(_.path.name == playerName).foreach(_ ! Character.Print(sender.path.name + " said " + message))
    case _ =>
  }
}

object PlayerManager {
  case class AddNewPlayer(player: String, sock: ServerSocket, br: BufferedReader, ps: PrintStream)
  case object Refresh
  case class TellOneUser(playerName: String, message: String)
}