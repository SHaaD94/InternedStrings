package com.github.shaad.internedstrings.benchmarks

import com.github.shaad.internedstrings.{
  BruteForceDiskBackedInternedStrings,
  DiskBinarySearchBackedInternedStrings,
  DiskHashBackedInternedStrings
}
import org.openjdk.jmh.annotations._

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.UUID
import scala.util.Random

@State(Scope.Benchmark)
class InternedStringsBenchMark {
  private val rand = new Random
  private val dataset = genDataset(10_000)

  private val dir = Files.createTempDirectory("bench").toFile

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
  def bruteForceDisk(): Unit = {
    val internedStrings =
      BruteForceDiskBackedInternedStrings.apply(dataset, Paths.get(dir.toString, UUID.randomUUID().toString))
    dataset.foreach(x => internedStrings.lookup(new String(x, StandardCharsets.UTF_8)))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
  def diskHash(): Unit = {
    val internedStrings =
      DiskHashBackedInternedStrings.apply(dataset, Paths.get(dir.toString, UUID.randomUUID().toString))
    dataset.foreach(x => internedStrings.lookup(new String(x, StandardCharsets.UTF_8)))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
  def diskBinarySearch(): Unit = {
    val internedStrings =
      DiskBinarySearchBackedInternedStrings.apply(dataset, Paths.get(dir.toString, UUID.randomUUID().toString))
    dataset.foreach(x => internedStrings.lookup(new String(x, StandardCharsets.UTF_8)))
  }

  @TearDown
  def removeDir(): Unit = {
    dir.listFiles().foreach(_.delete())
    dir.delete()
  }

  private def genDataset(size: Int): Array[Array[Byte]] = {
    ((0 to size).map(_ => rand.nextString(20).getBytes(StandardCharsets.UTF_8))).toArray
  }
}
