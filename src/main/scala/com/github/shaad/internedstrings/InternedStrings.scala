package com.github.shaad.internedstrings

trait InternedStrings {
  def lookup(id: Int): String
  def lookup(word: String): Int
}

object InternedStrings {
  val NullId: Int = -1
}
