package com.github.shaad.internedstrings
import com.github.shaad.internedstrings.InternedStrings.NullId
import com.github.shaad.internedstrings.LevelDBInternedStrings.toByteArray
import org.iq80.leveldb._
import org.iq80.leveldb.impl.Iq80DBFactory._

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Path

object LevelDBInternedStrings {
  def apply(strings: Array[Array[Byte]], filePath: Path): LevelDBInternedStrings = {
    val options = new Options()
    options.createIfMissing(true)
    options.cacheSize(8 * 1024)
    val db: DB = factory.open(filePath.toFile, options)

    val batch = db.createWriteBatch
    strings.zipWithIndex.foreach { case (s, i) =>
      batch.put(s, toByteArray(i))
      batch.put(toByteArray(i), s)
    }
    db.write(batch)
    batch.close()

    new LevelDBInternedStrings(db)
  }

  private def toByteArray(data: Int): Array[Byte] = {
    val result = new Array[Byte](4)
    result(0) = ((data & 0xff000000) >> 24).toByte
    result(1) = ((data & 0x00ff0000) >> 16).toByte
    result(2) = ((data & 0x0000ff00) >> 8).toByte
    result(3) = ((data & 0x000000ff) >> 0).toByte
    result
  }
}

class LevelDBInternedStrings private (db: DB) extends InternedStrings {
  override def lookup(id: Int): String = {
    val res = db.get(toByteArray(id))
    if (res == null) return null
    new String(res, StandardCharsets.UTF_8)
  }

  override def lookup(word: String): Int = {
    val res = db.get(word.getBytes(StandardCharsets.UTF_8))
    if (res == null) return NullId
    ByteBuffer.wrap(res).getInt
  }
}
