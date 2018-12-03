package mud

import scala.reflect.ClassTag

class BinaryHeap[A: ClassTag](compare: (A, A) => Boolean) {
  private var heap = new Array[A](10)
  private var end = 1

  def enqueue(element: A): Unit = {
    if (end >= heap.length) {
      val temp = new Array[A](heap.length * 2)
      Array.copy(heap, 0, temp, 0, heap.length)
      heap = temp
    }
    var bubble = end
    while (bubble > 1 && compare(element, heap(bubble / 2))) {
      heap(bubble) = heap(bubble / 2)
      bubble /= 2
    }
    heap(bubble) = element
    end += 1
  }

  def dequeue(): A = {
    val ret = heap(1)
    end = end - 1
    val temp = heap(end)
    heap(end) = heap(0)
    var stone = 1
    var flag = true
    while (flag && stone * 2 < end) {
      var greaterChild = if (stone * 2 + 1 < end && compare(heap(stone * 2 + 1), heap(stone * 2)))
        stone * 2 + 1 else stone * 2
      if (compare(heap(greaterChild), temp)) {
        heap(stone) = heap(greaterChild)
        stone = greaterChild
      } else {
        flag = false
      }
    }
    heap(stone) = temp
    ret
  }

  def peek: A = heap(1)

  def isEmpty: Boolean = end == 1

}