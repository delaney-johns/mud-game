package mud

import akka.actor.Actor
import akka.actor.Props

class NPCManager extends Actor {
  import NPCManager._

  def receive = {
    case MakeNewNPC =>
      val npc = context.actorOf(Props(new NPC("jeff_the_squirrel")), "jeff_the_squirrel")
      npc ! Character.RequestStartRoom
      Thread.sleep(100)
      npc ! Character.MoveNPC
    case _ =>
  }
}

object NPCManager {
  case object MakeNewNPC
}