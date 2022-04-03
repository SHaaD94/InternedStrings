package com.github.shaad.internedstrings

import com.github.shaad.internedstrings.InternedStrings.NullId
import com.koloboke.collect.map.hash.{HashObjIntMap, HashObjIntMaps}

class ArrayBackedInternedStrings(private val strings: Array[String]) extends InternedStrings {
  private val reverseMapping = {
    val map: HashObjIntMap[String] = HashObjIntMaps
      .newUpdatableMap[String](strings.length)

    for (i <- strings.indices) {
      map.put(strings(i), i)
    }

    map
  }

  override def lookup(id: Int): String =
    if (id == NullId) null else strings(id)

  override def lookup(v: String): Int =
    reverseMapping.getOrDefault(v, NullId)
}
