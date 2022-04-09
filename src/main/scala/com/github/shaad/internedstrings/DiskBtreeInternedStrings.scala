package com.github.shaad.internedstrings

import com.github.shaad.internedstrings.InternedStrings.NullId

import java.io.{BufferedOutputStream, DataOutputStream, File, RandomAccessFile}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption.{CREATE_NEW, WRITE}
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicInteger
import scala.math.{cbrt, ceil, max}
import scala.util.Using

case class Entry(stringIndex: Int, childNode: Option[Node])
case class Node(id: Int, entries: Array[Entry])

object DiskBtreeInternedStrings {
  def apply(strings: Array[Array[Byte]], filePath: Path): DiskBtreeInternedStrings = {
    // we want to not have more than 3 seeks for searching of each string
    // so we here we are solving equation log x (strings.length) = 3 | strings.length = x^3
    // but for the small arrays we don't want to make it too small, so we put hard minimum cap of 2
    val m = max(ceil(cbrt(strings.length)), 2)

    Using.resource(new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(filePath, CREATE_NEW, WRITE)))) {
      stream =>
        val sortedIndexes = strings.zipWithIndex.sorted(ArrayOrdering).map(_._2)
        val nodeIdCounter = new AtomicInteger(0)

        val nodeOffsets = new Array[Int](strings.length)
        val nodeLength = new Array[Int](strings.length)

        var curOffset = 0
        val offsets = new Array[Int](strings.length)

        def writeEntry(id: Int, entries: Array[(Int, Int)]): Int = {
          nodeOffsets(id) = curOffset

          stream.writeInt(entries.length)
          entries.foreach { case (stringIndex, childNode) =>
            stream.writeInt(stringIndex)
            stream.writeInt(childNode)
          }
          curOffset += 4 + 8 * entries.length
          entries.foreach { case (stringIndex, _) =>
            stream.write(strings(stringIndex))
            offsets(stringIndex) = curOffset
            curOffset += strings(stringIndex).length
          }
          nodeLength(id) = curOffset - nodeOffsets(id)
          id
        }

        def generateNode(l: Int, r: Int): Int = {
          if (l > r) {
            -1
          } else if (l == r) {
            writeEntry(nodeIdCounter.getAndIncrement(), Array((sortedIndexes(l), -1)))
          } else {
            val numbersInEntry = (r - l) / m.doubleValue
            if (numbersInEntry < 1) {
              writeEntry(nodeIdCounter.getAndIncrement(), (l to r).map(index => (sortedIndexes(index), -1)).toArray)
            } else {
              val entryRanges = (l to r).grouped(numbersInEntry.toInt + 1).toArray

              require(entryRanges.length <= m)

              writeEntry(
                nodeIdCounter.getAndIncrement(),
                entryRanges.map { r =>
                  (sortedIndexes(r.end), generateNode(r.start, r.end - 1))
                }
              )
            }
          }
        }

        generateNode(0, strings.length - 1)
        stream.flush()
        val lengths = strings.map(_.length)
        new DiskBtreeInternedStrings(
          filePath.toFile,
          offsets,
          lengths,
          curOffset,
          nodeOffsets.take(nodeIdCounter.get()),
          nodeLength.take(nodeIdCounter.get())
        )
    }
  }
}

case class NodeBytesWrapper(buffer: ByteBuffer) {
  lazy val numberOfEntries: Int = buffer.getInt(0)
  lazy val stringsSectionStart: Int = 4 + numberOfEntries * 8
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

  private val rootNode = readNodeInfo(raf, 0)

  override protected def getSize(id: Int): Int = lengths(id)

  override def lookup(word: String): Int = {
    val wordBytes = word.getBytes(StandardCharsets.UTF_8)

    var currentNodeInfo = rootNode
    while (currentNodeInfo != null) {
      var found = false
      var currentEntry = 0
      var bytesChecked = currentNodeInfo.stringsSectionStart
      while (!found && currentEntry < currentNodeInfo.numberOfEntries) {
        val stringIndex = currentNodeInfo.stringIndex(currentEntry)
        val bytesComparison = java.util.Arrays.compare(
          wordBytes,
          0,
          wordBytes.length,
          currentNodeInfo.buffer.array(),
          bytesChecked,
          bytesChecked + lengths(stringIndex)
        )
        if (bytesComparison < 0) {
          if (currentNodeInfo.childNode(currentEntry) != -1) {
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

    NullId
  }

  private def readNodeInfo(raf: RandomAccessFile, nodeId: Int): NodeBytesWrapper = {
    raf.seek(nodeOffsets(nodeId))
    val nodeBytes = new Array[Byte](nodeLengths(nodeId))
    raf.read(nodeBytes)
    val nodeInfo = NodeBytesWrapper(ByteBuffer.wrap(nodeBytes))
    nodeInfo
  }
}
