package com.github.shaad.internedstrings.benchmarks

import com.github.shaad.internedstrings._
import org.openjdk.jmh.annotations._

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.UUID
import scala.util.Random

@State(Scope.Benchmark)
class InternedStringsBenchMark {
  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  def bruteForceDisk(state: BruteForceState): Unit = standardBench(state)

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  def diskHash(state: HashState): Unit = standardBench(state)

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  def diskBinarySearch(state: BinSearchState): Unit = standardBench(state)

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  def diskBtree(state: BtreeState): Unit = standardBench(state)

  private def standardBench(state: BaseState): Unit = {
    val randomIndex = state.rand.nextInt(state.dataset.length)
    val randomString = state.dataset(randomIndex)
    state.data.lookup(new String(randomString, StandardCharsets.UTF_8))
  }
}

@State(Scope.Benchmark)
abstract class BaseState() {
  @Param(Array("1000", "10000", "100000", "1000000"))
  var stringsCount: Int = 0

  lazy val dataset: Array[Array[Byte]] = genDataset()
  var data: InternedStrings = null
  protected var dir: File = null

  val rand = new Random(0)

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

  private def genDataset(): Array[Array[Byte]] = {
    require(stringsCount > 0)
    (0 to stringsCount).map(_ => rand.nextString(20).getBytes(StandardCharsets.UTF_8)).toArray
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
