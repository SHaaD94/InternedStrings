package com.github.shaad.internedstrings

case class Entry(keyFunction: () => Array[Byte], index: Int /*value*/, nextNode: Option[Node])

case class Node(size: Int) {
  val children = new Array[Entry](size)
}

class MyBtree(private val m: Int) {
  var root: Node = Node(m)
  var height: Int = 0

  def put(index: Int) = {}

  def put(key: Key, `val`: Value): Unit = {
    if (key == null) throw new IllegalArgumentException("argument key to put() is null")
    val u = insert(root, key, `val`, height)
    if (u == null) return
    // need to split root
    val t = new Node(2)
    t.children(0) = new Entry(root.children(0).keyFunction, null, Some(root))
    t.children(1) = new Entry(u.children(0).keyFunction, null, Some(u))
    root = t
    height += 1
  }

  private def insert(h: Node, key: Key, `val`: Value, ht: Int): Node = {
    var j = 0
    val t = new Entry(key, `val`, null)
    // external node
    if (ht == 0) {
      j = 0
      while (j < h.m) {
        if (less(key, h.children(j).key)) break //todo: break is not supported

        j += 1
      }
    } else { // internal node
      j = 0
      while ({
        j < h.m
      }) {
        if ((j + 1 == h.m) || less(key, h.children(j + 1).key)) {
          val u = insert(
            h.children({
              j += 1; j - 1
            }).next,
            key,
            `val`,
            ht - 1
          )
          if (u == null) return null
          t.key = u.children(0).key
          t.`val` = null
          t.next = u
          break //todo: break is not supported

        }

        j += 1
      }
    }
    for (i <- h.m until j by -1) {
      h.children(i) = h.children(i - 1)
    }
    h.children(j) = t
    h.m += 1
    if (h.m < M) null
    else split(h)
  }

  // split node in half
  private def split(h: Node) = {
    val t = new Node(M / 2)
    h.m = M / 2
    for (j <- 0 until M / 2) {
      t.children(j) = h.children(M / 2 + j)
    }
    t
  }

}
