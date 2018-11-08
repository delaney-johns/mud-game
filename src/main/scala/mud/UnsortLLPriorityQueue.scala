package mud

class UnsortLLPriorityQueue[A](compareHigherPriority:(A, A) => Boolean) {
  private val dummyNode = new Node(null, null.asInstanceOf[A], null)
  dummyNode.next = dummyNode
  dummyNode.prev = dummyNode
  def peek: A = findHighestPriorityItem.data
  def isEmpty: Boolean = dummyNode.next == dummyNode
  def enqueue(a: A): Unit = {
    val n = new Node(dummyNode.prev, a, dummyNode)
    dummyNode.prev.next = n
    dummyNode.prev = n
  }
  def dequeue(): A = {
    val highestPriorityNode = findHighestPriorityItem
    highestPriorityNode.prev.next = highestPriorityNode.next
    highestPriorityNode.next.prev = highestPriorityNode.prev
    highestPriorityNode.data
  }
  private def findHighestPriorityItem: Node = {
    var walkerThroughList = dummyNode.next
    var highestPrioritySoFar = dummyNode.next
    while(walkerThroughList != dummyNode) {
      if (compareHigherPriority(walkerThroughList.data, highestPrioritySoFar.data)) {
    	  highestPrioritySoFar = walkerThroughList
        
      }
      walkerThroughList = walkerThroughList.next
    }
    highestPrioritySoFar
  }
  
  private class Node(var prev: Node, val data: A, var next: Node)
} 
  
