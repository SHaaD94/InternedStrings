package com.github.shaad.internedstrings

import com.github.shaad.internedstrings.InternedStrings.NullId

class SimpleInternedStrings(private val strings: Array[String]) extends InternedStrings {
  private val reverseMapping = strings.zipWithIndex.toMap

  override def lookup(id: Int): String =
    if (id == NullId) null else strings(id)

  override def lookup(v: String): Int =
    reverseMapping.getOrElse(v, NullId)
}
