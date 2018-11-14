package mud

import akka.actor.Actor
import akka.actor.Props

class NPCManager extends Actor {
  import NPCManager._

  def receive = {
    case MakeNewNPC =>
      val npc = context.actorOf(Props(new NPC("jeff_the_squirrel", 35)), "jeff_the_squirrel")
      npc ! Characters.RequestStartRoom
    case _ =>
  }
}

object NPCManager {
  case object MakeNewNPC
}