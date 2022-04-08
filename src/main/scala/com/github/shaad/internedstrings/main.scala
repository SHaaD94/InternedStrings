package com.github.shaad.internedstrings

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.UUID
import scala.util.Random

object main extends App {
  val rand = new Random
  val dataset = (0 to 100_000).map(_ => rand.nextString(20).getBytes(StandardCharsets.UTF_8)).toArray
  val tempdir = Files.createTempDirectory("bench").toFile

  val a = (0 to 300).map { i =>
    println(i)
    val b = DiskBtreeInternedStrings.apply(dataset, Paths.get(tempdir.toString, UUID.randomUUID().toString))

//    val b =
//      DiskBinarySearchBackedInternedStrings.apply(dataset, Paths.get(tempdir.toString, UUID.randomUUID().toString))
    //    BruteForceDiskBackedInternedStrings.apply(dataset, Paths.get(tempdir.toString, UUID.randomUUID().toString))
    //    DiskHashBackedInternedStrings.apply(dataset, Paths.get(tempdir.toString, UUID.randomUUID().toString))
//    dataset.foreach(x => b.lookup(new String(x)))
    b
  }

  Thread.sleep(1000000000)
}
// 1.3 GB HASH
// 271 MB BTree
// 245 MB Binary
// 128 MB Bruteforce
