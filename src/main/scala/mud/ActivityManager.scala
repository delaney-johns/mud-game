package mud

import akka.actor.Actor
import akka.actor.ActorRef

class ActivityManager extends Actor {
  import ActivityManager._

  //priority queue adds activities and evaluates based on what must happen first (shortest time)
  private var counter = 0
  private var priorityQueue = new UnsortLLPriorityQueue[Activity]((a, b) => a.time < b.time)
  def receive = {
    case CheckInput =>     
      counter += 1
      while (!priorityQueue.isEmpty && priorityQueue.peek.time == counter) {
        val Activity = priorityQueue.peek
        priorityQueue.dequeue()
        Activity.sender ! Activity.message
      }
    case Enqueue(time, message) => 
      priorityQueue.enqueue(Activity(time + counter, sender, message))

    case _ =>
  }
}

object ActivityManager {
  case object CheckInput
  case class Activity(time: Int, sender: ActorRef, message: Any)
  case class Enqueue(time: Int, message: Any)
}