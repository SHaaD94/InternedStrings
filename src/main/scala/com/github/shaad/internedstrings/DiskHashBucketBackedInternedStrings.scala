package com.github.shaad.internedstrings

import com.github.shaad.internedstrings.InternedStrings.NullId
import com.koloboke.collect.map.hash.{HashIntIntMap, HashIntIntMaps, HashIntObjMap, HashIntObjMaps}
import com.koloboke.collect.set.hash.{HashIntSet, HashIntSets}

import java.io.{BufferedOutputStream, DataOutputStream, File}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.nio.file.StandardOpenOption.{CREATE_NEW, WRITE}
import java.util
import java.util.function.IntFunction
import scala.collection.mutable
import scala.util.Using

object DiskHashBucketBackedInternedStrings {
  def apply(
      strings: Array[Array[Byte]],
      filePath: Path,
      expectedElementsInBucket: Int = 10
  ): DiskHashBucketBackedInternedStrings = {
    Using.resource(new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(filePath, CREATE_NEW, WRITE)))) {
      stream =>
        val buckets: Array[mutable.ArrayBuffer[Int]] =
          (0 until math.max(strings.length / expectedElementsInBucket, 1)).map { _ =>
            mutable.ArrayBuffer[Int]()
          }.toArray

        val offsets = new Array[Int](strings.length)
        val lengths = strings.map(_.length)
        strings.zipWithIndex.foreach { case (string, index) =>
          val stringHash = math.abs(util.Arrays.hashCode(string))
          buckets(stringHash % buckets.length) += index
        }
        var currentOffset = 0
        buckets.foreach { bucket =>
          bucket.foreach { index =>
            offsets(index) = currentOffset
            currentOffset += strings(index).length
            stream.write(strings(index))
          }
        }

        stream.flush()
        new DiskHashBucketBackedInternedStrings(
          filePath.toFile,
          offsets,
          lengths,
          buckets.map(_.toArray),
          currentOffset
        )
    }
  }
}

class DiskHashBucketBackedInternedStrings private (
    file: File,
    offsets: Array[Int],
    private val lengths: Array[Int],
    private val hashBuckets: Array[Array[Int]],
    private val totalSize: Int
) extends BaseDiskInternedStrings(file, offsets, totalSize) {

  override protected def getSize(id: Int): Int = lengths(id)

  override def lookup(word: String): Int = {
    val wordBytes = word.getBytes(StandardCharsets.UTF_8)
    val wordHashCode = math.abs(util.Arrays.hashCode(wordBytes))
    val bucket = hashBuckets(wordHashCode % hashBuckets.length)

    bucket.headOption match {
      case None => NullId
      case Some(idOfFirstStringIngBucket) =>
        val bucketBytes: Array[Byte] = readBucket(bucket, idOfFirstStringIngBucket)
        var requiredIndex: Int = NullId
        var arrayOffset = 0
        var i = 0
        while (requiredIndex == NullId && i < bucket.length) {
          val currentStringIndex = bucket(i)
          if (
            util.Arrays.compare(
              wordBytes,
              0,
              wordBytes.length,
              bucketBytes,
              arrayOffset,
              arrayOffset + lengths(currentStringIndex)
            ) == 0
          ) {
            requiredIndex = currentStringIndex
          }
          arrayOffset += lengths(currentStringIndex)
          i += 1
        }
        requiredIndex
    }
  }

  private def readBucket(bucket: Array[Int], idOfFirstStringIngBucket: Int): Array[Byte] = {
    raf.seek(offsets(idOfFirstStringIngBucket))
    val bucketBytes = new Array[Byte](bucket.map(lengths(_)).sum)
    raf.readFully(bucketBytes)
    bucketBytes
  }
}
