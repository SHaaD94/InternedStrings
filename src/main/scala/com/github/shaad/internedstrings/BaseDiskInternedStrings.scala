package com.github.shaad.internedstrings

import com.github.shaad.internedstrings.InternedStrings.NullId

import java.io.{File, RandomAccessFile}
import java.nio.charset.StandardCharsets
import scala.collection.concurrent.TrieMap
import scala.util.Using

abstract class BaseDiskInternedStrings(
    private val file: File,
    private val offsets: Array[Int],
    private val totalSize: Int
) extends InternedStrings {
  protected val string2Id = new TrieMap[String, Int]()
  protected val id2String = new TrieMap[Int, String]()

  override def lookup(id: Int): String = {
    if (id == NullId || offsets.length <= id) {
      null
    } else {
      id2String.getOrElseUpdate(
        id, {
          Using.resource(new RandomAccessFile(file, "r")) { raf =>
            val bytes = readBytesByIndex(raf, id)
            val string = new String(bytes, StandardCharsets.UTF_8)
            string2Id.put(string, id)
            string
          }
        }
      )
    }
  }

  protected def readBytesByIndex(raf: RandomAccessFile, index: Int): Array[Byte] = {
    raf.seek(offsets(index))
    val bytes = new Array[Byte](getSize(index))
    raf.read(bytes)
    bytes
  }

  protected def getSize(id: Int): Int =
    if (id == offsets.length - 1) {
      if (offsets.length == 1) totalSize else totalSize - offsets(id)
    } else {
      offsets(id + 1) - offsets(id)
    }

}
