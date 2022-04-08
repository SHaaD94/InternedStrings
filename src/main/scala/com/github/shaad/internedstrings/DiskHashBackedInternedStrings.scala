package com.github.shaad.internedstrings

import com.github.shaad.internedstrings.InternedStrings.NullId
import com.koloboke.collect.map.hash.{HashIntIntMap, HashIntIntMaps, HashIntObjMap, HashIntObjMaps}
import com.koloboke.collect.set.hash.{HashIntSet, HashIntSets}

import java.io.{BufferedOutputStream, DataOutputStream, File, RandomAccessFile}
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption.{CREATE_NEW, WRITE}
import java.nio.file.{Files, Path}
import java.util
import java.util.function.IntFunction
import scala.util.Using

object DiskHashBackedInternedStrings {
  def apply(strings: Array[Array[Byte]], filePath: Path): DiskHashBackedInternedStrings = {
    Using.resource(new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(filePath, CREATE_NEW, WRITE)))) {
      stream =>
        val offsets = new Array[Int](strings.length)
        val hash2Offset = HashIntIntMaps.newMutableMap()
        val hash2MultipleOffsets = HashIntObjMaps.newMutableMap[HashIntSet]()

        var currentOffset = 0
        strings.zipWithIndex.foreach { case (string, index) =>
          stream.write(string)
          offsets(index) = currentOffset
          currentOffset += string.length
          val stringHash = util.Arrays.hashCode(string)
          if (hash2Offset.containsKey(stringHash)) {
            hash2Offset.remove(stringHash)
            hash2MultipleOffsets
              .computeIfAbsent(
                stringHash,
                new IntFunction[HashIntSet] {
                  override def apply(value: Int): HashIntSet = HashIntSets.newUpdatableSet()
                }
              )
              .add(index)

          } else {
            hash2Offset.put(stringHash, index)
          }
        }

        stream.flush()
        new DiskHashBackedInternedStrings(filePath.toFile, offsets, hash2Offset, hash2MultipleOffsets, currentOffset)
    }
  }
}

class DiskHashBackedInternedStrings private (
    private val file: File,
    private val offsets: Array[Int],
    private val hash2Offset: HashIntIntMap,
    private val hash2MultipleOffsets: HashIntObjMap[HashIntSet],
    private val totalSize: Int
) extends BaseDiskInternedStrings(file, offsets, totalSize) {

  override def lookup(word: String): Int = {
//    string2Id.getOrElseUpdate(
//      word, {
    val wordBytes = word.getBytes(StandardCharsets.UTF_8)
    val wordHashCode = util.Arrays.hashCode(wordBytes)
    val onlyIndex = hash2Offset.getOrDefault(wordHashCode, NullId)
    if (onlyIndex != NullId) {
      val bytes = readBytesByIndex(raf, onlyIndex)
      if (util.Arrays.compare(wordBytes, bytes) == 0) {
//            id2String.put(onlyIndex, new String(bytes, StandardCharsets.UTF_8))
        onlyIndex
      } else {
        NullId
      }
    } else {
      val potentialCells = hash2MultipleOffsets.get(wordHashCode)
      if (potentialCells == null) {
        NullId
      } else {
        val cellsCursor = potentialCells.cursor()
        var requiredIndex: Int = NullId
        while (requiredIndex == NullId && cellsCursor.moveNext()) {
          val index = cellsCursor.elem()
          val bytes = readBytesByIndex(raf, index)
          if (util.Arrays.compare(wordBytes, bytes) == 0) {
//                id2String.put(index, new String(bytes, StandardCharsets.UTF_8))
            requiredIndex = index
          }
        }
        requiredIndex
      }
    }
//      }
//    )
  }
}
