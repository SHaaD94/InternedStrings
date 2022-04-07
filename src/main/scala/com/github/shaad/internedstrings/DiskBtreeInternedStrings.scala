package com.github.shaad.internedstrings

import com.github.shaad.internedstrings.InternedStrings.NullId

import java.io.{BufferedOutputStream, DataOutputStream, File, RandomAccessFile}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption.{CREATE_NEW, WRITE}
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable
import scala.util.Using

case class Entry(stringIndex: Int, childNode: Option[Node])
case class Node(id: Int, entries: Array[Entry])

object DiskBtreeInternedStrings {
  def apply(strings: Array[Array[Byte]], filePath: Path, m: Int): DiskBtreeInternedStrings = {
    require(m > 1, "Invalid branching factor")

    Using.resource(new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(filePath, CREATE_NEW, WRITE)))) {
      stream =>
        val sortedIndexes = strings.zipWithIndex.sorted(ArrayOrdering).map(_._2)
        val nodeIdCounter = new AtomicInteger(0)

        def createNode(l: Int, r: Int): Option[Node] = {
          if (l > r) {
            None
          } else if (l == r) {
            Some(Node(nodeIdCounter.getAndIncrement(), Array(Entry(sortedIndexes(l), None))))
          } else {
            val numbersInEntry = (r - l) / m.doubleValue
            if (numbersInEntry < 1) {
              Some(
                Node(nodeIdCounter.getAndIncrement(), (l to r).map(index => Entry(sortedIndexes(index), None)).toArray)
              )
            } else {
              val entryRanges = (l to r).grouped(numbersInEntry.toInt + 1).toArray

              require(entryRanges.length <= m)

              Some(
                Node(
                  nodeIdCounter.getAndIncrement(),
                  entryRanges.map { r =>
                    Entry(sortedIndexes(r.end), createNode(r.start, r.end - 1))
                  }
                )
              )
            }
          }
        }

        val rootNode = createNode(0, sortedIndexes.length - 1).get
        val nodeOffsets = new Array[Int](nodeIdCounter.get())
        val nodeLength = new Array[Int](nodeIdCounter.get())
        val nodeWritingQueue = mutable.Queue(rootNode)
        var curOffset = 0
        val offsets = new Array[Int](strings.length)
        while (nodeWritingQueue.nonEmpty) {
          val curNode = nodeWritingQueue.dequeue()
          nodeOffsets(curNode.id) = curOffset
          stream.writeInt(curNode.entries.length)
          curNode.entries.foreach { entry =>
            stream.writeInt(entry.stringIndex)
            stream.writeInt(entry.childNode.map(x => x.id).getOrElse(-1))
          }
          curOffset += 4 + 8 * curNode.entries.length
          curNode.entries.foreach { entry =>
            stream.write(strings(entry.stringIndex))
            offsets(entry.stringIndex) = curOffset
            curOffset += strings(entry.stringIndex).length
            entry.childNode.foreach(nodeWritingQueue.addOne)
          }
          nodeLength(curNode.id) = curOffset - nodeOffsets(curNode.id)
        }

        stream.flush()
        val lengths = strings.map(_.length)
        new DiskBtreeInternedStrings(filePath.toFile, offsets, lengths, curOffset, nodeOffsets, nodeLength)
    }
  }
}

case class NodeBytesWrapper(buffer: ByteBuffer) {
  lazy val numberOfEntries: Int = buffer.getInt(0)
  def stringIndex(entryId: Int): Int = buffer.getInt(4 + entryId * 8)
  def childNode(entryId: Int): Int = buffer.getInt(4 + entryId * 8 + 4)
}

class DiskBtreeInternedStrings private (
    private val file: File,
    private val offsets: Array[Int],
    private val lengths: Array[Int],
    private val totalSize: Int,
    private val nodeOffsets: Array[Int],
    private val nodeLengths: Array[Int]
) extends BaseDiskInternedStrings(file, offsets, totalSize) {

  override protected def getSize(id: Int): Int = lengths(id)

  override def lookup(word: String): Int = {
    Using.resource(new RandomAccessFile(file, "r")) { raf =>
      val wordBytes = word.getBytes(StandardCharsets.UTF_8)

      var currentNodeInfo = readNodeInfo(raf, 0) // read root node
      while (currentNodeInfo != null) {
        var found = false
        var currentEntry = 0
        var bytesChecked = 4 + currentNodeInfo.numberOfEntries * 8
        while (!found && currentEntry < currentNodeInfo.numberOfEntries) {
          val stringIndex = currentNodeInfo.stringIndex(currentEntry)
          val bytesComparison = java.util.Arrays.compare(
            wordBytes,
            0,
            word.length,
            currentNodeInfo.buffer.array(),
            bytesChecked,
            bytesChecked + lengths(stringIndex)
          )
          if (bytesComparison < 0) {
            if (currentNodeInfo.childNode(currentEntry) != -1) {
              raf.seek(nodeOffsets(currentNodeInfo.childNode(currentEntry)))
              currentNodeInfo = readNodeInfo(raf, currentNodeInfo.childNode(currentEntry))
            } else {
              currentNodeInfo = null
            }
            found = true
          } else if (bytesComparison > 0 && currentNodeInfo.numberOfEntries == currentEntry + 1) {
            return NullId
          } else if (bytesComparison == 0) {
            return stringIndex
          }

          bytesChecked += lengths(stringIndex)
          currentEntry += 1
        }
      }
      return NullId
    }
  }

  private def readNodeInfo(raf: RandomAccessFile, nodeId: Int): NodeBytesWrapper = {
    val nodeBytes = new Array[Byte](nodeLengths(nodeId))
    raf.read(nodeBytes)
    val nodeInfo = NodeBytesWrapper(ByteBuffer.wrap(nodeBytes))
    nodeInfo
  }
}
