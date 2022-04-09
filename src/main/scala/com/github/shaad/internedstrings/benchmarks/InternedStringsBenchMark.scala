package com.github.shaad.internedstrings.benchmarks

import com.github.shaad.internedstrings.{
  BruteForceDiskBackedInternedStrings,
  DiskBinarySearchBackedInternedStrings,
  DiskBtreeInternedStrings,
  DiskHashBackedInternedStrings,
  InternedStrings
}
import org.openjdk.jmh.annotations._

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.UUID
import scala.util.Random

@State(Scope.Benchmark)
class InternedStringsBenchMark {
  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
  def bruteForceDisk(state: BruteForceState): Unit = {
    state.dataset.foreach(x => state.data.lookup(new String(x, StandardCharsets.UTF_8)))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
  def diskHash(state: HashState): Unit = {
    state.dataset.foreach(x => state.data.lookup(new String(x, StandardCharsets.UTF_8)))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
  def diskBinarySearch(state: BinSearchState): Unit = {
    state.dataset.foreach(x => state.data.lookup(new String(x, StandardCharsets.UTF_8)))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
  def diskBtree(state: BtreeState): Unit = {
    state.dataset.foreach(x => state.data.lookup(new String(x, StandardCharsets.UTF_8)))
  }
}

@State(Scope.Benchmark)
abstract class BaseState() {
  var stringsCount: Int = 100_000

  val dataset: Array[Array[Byte]] = genDataset(stringsCount)
  var data: InternedStrings = null
  protected var dir: File = null

  @Setup(value = Level.Iteration)
  def init(): Unit = {
    dir = Files.createTempDirectory("bench").toFile
    val filepath = Paths.get(dir.toString, UUID.randomUUID().toString)
    data = createStrings(filepath)
  }

  def createStrings(file: Path): InternedStrings

  def removeDir(): Unit = {
    dir.listFiles().foreach(_.delete())
    dir.delete()
  }

  private def genDataset(size: Int): Array[Array[Byte]] = {
    val rand = new Random(0)

    (0 to size).map(_ => rand.nextString(20).getBytes(StandardCharsets.UTF_8)).toArray
  }
}

class BruteForceState extends BaseState {
  override def createStrings(file: Path): InternedStrings = BruteForceDiskBackedInternedStrings.apply(dataset, file)
}

class HashState extends BaseState {
  override def createStrings(file: Path): InternedStrings = DiskHashBackedInternedStrings.apply(dataset, file)
}

class BinSearchState extends BaseState {
  override def createStrings(file: Path): InternedStrings = DiskBinarySearchBackedInternedStrings.apply(dataset, file)
}

class BtreeState extends BaseState {
  override def createStrings(file: Path): InternedStrings = DiskBtreeInternedStrings.apply(dataset, file)
}
