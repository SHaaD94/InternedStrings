package com.github.shaad.internedstrings

import com.github.shaad.internedstrings.InternedStrings.NullId

import java.io.{BufferedOutputStream, DataOutputStream, File}
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption.{CREATE_NEW, READ, WRITE}
import java.nio.file.{Files, Path}
import scala.annotation.tailrec
import scala.util.Using

object BruteForceDiskBackedInternedStrings {
  def apply(strings: Array[Array[Byte]], filePath: Path): BruteForceDiskBackedInternedStrings = {
    Using.resource(new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(filePath, CREATE_NEW, WRITE)))) {
      stream =>
        val offsets = new Array[Int](strings.length)

        var currentOffset = 0
        strings.zipWithIndex.foreach { case (string, index) =>
          stream.write(string)
          offsets(index) = currentOffset
          currentOffset += string.length
        }

        stream.flush()
        new BruteForceDiskBackedInternedStrings(filePath.toFile, offsets, currentOffset)
    }
  }
}

class BruteForceDiskBackedInternedStrings private (
    private val file: File,
    private val offsets: Array[Int],
    private val totalSize: Int
) extends BaseDiskInternedStrings(file, offsets, totalSize) {
  override def lookup(word: String): Int = {
    Using.resource(Files.newInputStream(file.toPath, READ)) { is =>
      val wordBytes = word.getBytes(StandardCharsets.UTF_8)

      @tailrec
      def search(index: Int): Int = {
        if (index >= offsets.length) {
          NullId
        } else {
          val bytes = new Array[Byte](getSize(index))
          is.read(bytes)
          if (java.util.Arrays.compare(wordBytes, bytes) == 0) index else search(index + 1)
        }
      }

      search(0)
    }
  }
}
