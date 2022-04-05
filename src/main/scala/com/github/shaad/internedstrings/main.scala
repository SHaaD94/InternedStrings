package com.github.shaad.internedstrings

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.util.Random

object main extends App {
  val rand = new Random
  val dataset = (0 to 100_000).map(_ => rand.nextString(20).getBytes(StandardCharsets.UTF_8)).toArray
  val tempdir = Files.createTempDirectory("bench").toFile

  val a = (0 to 300).map { i =>
    println(i)
//    DiskHashBackedInternedStrings.apply(dataset, Paths.get(tempdir.toString, UUID.randomUUID().toString))
//    DiskBinarySearchBackedInternedStrings.apply(dataset, Paths.get(tempdir.toString, UUID.randomUUID().toString))
//    BruteForceDiskBackedInternedStrings.apply(dataset, Paths.get(tempdir.toString, UUID.randomUUID().toString))
  }

  Thread.sleep(1000000000)
}
//5,014,179,384 B - Hash
//  257,846,296 B - BinSearch
//  137,445,448 B - Bruteforce
//   16,800 000