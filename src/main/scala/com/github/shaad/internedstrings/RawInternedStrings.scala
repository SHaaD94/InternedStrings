package com.github.shaad.internedstrings

import com.github.shaad.internedstrings.InternedStrings.NullId

import java.util
import scala.collection.concurrent.TrieMap

class RawInternedStrings(private val strings: Array[Array[Byte]]) extends InternedStrings {
  private val string2Id = new TrieMap[String, Int]()

  override def lookup(id: Int): String = {
    if (id == NullId) {
      null
    } else {
      new String(strings(id))
    }
  }

  override def lookup(word: String): Int = {
    string2Id.getOrElseUpdate(
      word, {
        val wordBytes = word.getBytes()
        var i = 1
        var found = false
        while (!found && i < strings.length) {
          if (util.Arrays.compare(wordBytes, strings(i)) == 0) {
            found = true
          } else {
            i += 1
          }
        }
        if (found) i else NullId
      }
    )
  }
}
