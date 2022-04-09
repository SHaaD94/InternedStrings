package com.github.shaad.internedstrings

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.UUID

abstract class InternedStringsSpec extends AnyWordSpec with BeforeAndAfterAll {
  private val strings: Array[Array[Byte]] =
    Array("a", "bbb", "cccccc", "qweqweqwerasd", "234243", "!3afas432", "", " ", "hgsdg asd").map(
      _.getBytes(StandardCharsets.UTF_8)
    )
  private val directory = Files.createTempDirectory("temp-test-dir").toFile

  "InternedStrings" should {
    val internedStrings = initStrings(strings, Paths.get(directory.toString, UUID.randomUUID().toString))
    // id lookup
    "find first value by id properly" in {
      internedStrings
        .lookup(0) mustBe "a"
    }
    "find last value by id properly" in {
      internedStrings
        .lookup(8) mustBe "hgsdg asd"
    }
    "find empty value by id properly" in {
      internedStrings
        .lookup(6) mustBe ""
    }
    "find blank value by id properly" in {
      internedStrings
        .lookup(7) mustBe " "
    }
    "find null value by 0 id properly" in {
      internedStrings
        .lookup(-1) mustBe null
    }
    // string lookup
    "find id by empty string properly" in {
      internedStrings
        .lookup("") mustBe 6
    }
    "find id by blank string properly" in {
      internedStrings
        .lookup(" ") mustBe 7
    }
    "find id of last string properly" in {
      internedStrings
        .lookup("hgsdg asd") mustBe 8
    }
    "find id of first string properly" in {
      internedStrings
        .lookup("a") mustBe 0
    }
    "find id of random string properly" in {
      internedStrings
        .lookup("234243") mustBe 4
    }
    "should not find id of missing string properly" in {
      internedStrings
        .lookup("NOT EXISTING STRING") mustBe -1
    }
  }

  def initStrings(strings: Array[Array[Byte]], filePath: Path): InternedStrings

  override protected def afterAll(): Unit = {
    directory.listFiles.foreach(_.delete())
    directory.delete()
  }
}

class SimpleInternedStringsSpec extends InternedStringsSpec {
  override def initStrings(strings: Array[Array[Byte]], filePath: Path): InternedStrings =
    new SimpleInternedStrings(strings.map(new String(_)))
}

class ArrayBackedInternedStringsSpec extends InternedStringsSpec {
  override def initStrings(strings: Array[Array[Byte]], filePath: Path): InternedStrings =
    new ArrayBackedInternedStrings(strings.map(new String(_)))
}

class HashDiskInternedStringsSpec extends InternedStringsSpec {
  override def initStrings(strings: Array[Array[Byte]], filePath: Path): InternedStrings =
    DiskHashBackedInternedStrings.apply(strings, filePath)
}

class BinarySearchDiskInternedStringsSpec extends InternedStringsSpec {
  override def initStrings(strings: Array[Array[Byte]], filePath: Path): InternedStrings =
    DiskBinarySearchBackedInternedStrings.apply(strings, filePath)
}

class BruteForceDiskBackedInternedStringsSpec extends InternedStringsSpec {
  override def initStrings(strings: Array[Array[Byte]], filePath: Path): InternedStrings =
    BruteForceDiskBackedInternedStrings.apply(strings, filePath)
}

class BTreeIndexDiskSpec extends InternedStringsSpec {
  override def initStrings(strings: Array[Array[Byte]], filePath: Path): InternedStrings =
    DiskBtreeInternedStrings.apply(strings, filePath)
}
