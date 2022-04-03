package com.github.shaad.internedstrings

import java.io.{File, RandomAccessFile}
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption.{CREATE_NEW, WRITE}
import java.nio.file.{Files, Path}
import java.util
import scala.annotation.tailrec
import scala.util.Using

object ArrayOrdering extends Ordering[(Array[Byte], Int)] {
  override def compare(x: (Array[Byte], Int), y: (Array[Byte], Int)): Int = {
    util.Arrays.compare(x._1, y._1)
  }
}

object DiskBinarySearchBackedInternedStrings {
  def apply(strings: Array[Array[Byte]], filePath: Path): DiskBinarySearchBackedInternedStrings = {
    Using.resource(Files.newOutputStream(filePath, CREATE_NEW, WRITE)) { stream =>
      val offsets = new Array[Int](strings.length)
      val sortedMappings = new Array[Int](strings.length)

      strings.zipWithIndex
        .sorted(ArrayOrdering)
        .zipWithIndex
        .foreach { case ((_, originalIndex), sortedIndex) =>
          sortedMappings(sortedIndex) = originalIndex
        }

      var currentOffset = 0
      strings.zipWithIndex.foreach { case (string, originalIndex) =>
        stream.write(string)
        offsets(originalIndex) = currentOffset
        currentOffset += string.length
      }

      stream.flush()
      new DiskBinarySearchBackedInternedStrings(
        filePath.toFile,
        offsets,
        sortedMappings,
        currentOffset
      )
    }
  }
}

class DiskBinarySearchBackedInternedStrings(
    private val file: File,
    private val offsets: Array[Int],
    private val sortedIdsMapping: Array[Int],
    private val totalSize: Int
) extends BaseDiskInternedStrings(file, offsets, totalSize) {
  override def lookup(word: String): Int = {
    string2Id.getOrElseUpdate(
      word, {
        Using.resource(new RandomAccessFile(file, "r")) { raf =>
          binsearch(0, offsets.length - 1, word.getBytes(StandardCharsets.UTF_8), raf)
        }
      }
    )
  }

  @tailrec
  private def binsearch(
      l: Int,
      r: Int,
      target: Array[Byte],
      raf: RandomAccessFile
  ): Int = {
    if (r >= l) {
      val midIdx = l + (r - l) / 2
      val mid = sortedIdsMapping(midIdx)
      val candidate = readBytesByIndex(raf, mid)
      val c = util.Arrays.compare(target, candidate)
      if (c == 0) {
        mid
      } else if (c < 0) {
        binsearch(l, midIdx - 1, target, raf)
      } else {
        binsearch(midIdx + 1, r, target, raf)
      }
    } else {
      InternedStrings.NullId
    }
  }
}
