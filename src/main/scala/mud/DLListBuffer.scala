package mud

import scala.collection.mutable.Buffer

class DLListBuffer [A] extends Buffer[A] {
  import DLLBuffer.Node

  private var end: Node[A] = new Node[A](null, null.asInstanceOf[A], null)
  end.next = end
  end.prev = end
  private var numElems = 0

  def +=(elem: A) = {
    val n = new Node[A](end.prev, elem, end)
    end.prev.next = n
    end.prev = n
    numElems += 1
    this
  }
  def +=:(elem: A) = {
    val n = new Node[A](end, elem, end.next)
    end.next.prev = n
    end.next = n
    numElems += 1
    this
  }
  def apply(n: Int): A = {
    require(n < length && n >= 0)
    var rover = end.next
    for (_ <- 0 until n) rover = rover.next
    rover.data
  }
  def clear(): Unit = {
    end.next = end
    end.prev = end
    numElems = 0
  }
  def insertAll(n: Int, elems: Traversable[A]): Unit = {
    require(n <= length && n >= 0)
    var rover = end.next
    for (_ <- 0 until n) rover = rover.next
    for (e <- elems) {
      val n = new Node(rover.prev, e, rover)
      rover.prev.next = n
      rover.prev = n
      numElems += 1
    }
  }
  def length: Int = numElems
  def remove(n: Int): A = {
    require(n < length && n >= 0)
    numElems -= 1
    var rover = end.next
    for (_ <- 0 until n) rover = rover.next
    val ret = rover.data
    rover.next.prev = rover.prev
    rover.prev.next = rover.next
    ret
  }
  def update(n: Int, newElem: A): Unit = {
    require(n < length && n >= 0)
    var rover = end.next
    for (_ <- 0 until n) rover = rover.next
    rover.data = newElem
  }

  // Members declared in scala.collection.IterableLike
  def iterator: Iterator[A] = new Iterator[A] {
    private var rover = end.next
    def hasNext: Boolean = rover != end
    def next(): A = {
      val ret = rover.data
      rover = rover.next
      ret
    }
  }
}

object DLLBuffer {
  class Node[A](var prev: Node[A], var data: A, var next: Node[A])
}