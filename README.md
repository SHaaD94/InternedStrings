# Interned Strings

Comparison of different implementations of `string->int` dictionaries

### Benchmark results
```
[info] Benchmark                                  (stringsCount)   Mode  Cnt       Score       Error  Units
[info] InternedStringsBenchMark.bruteForceDisk              1000  thrpt   10    2218.065 ±   182.307  ops/s
[info] InternedStringsBenchMark.bruteForceDisk             10000  thrpt   10     239.072 ±    14.664  ops/s
[info] InternedStringsBenchMark.bruteForceDisk            100000  thrpt   10      22.364 ±     1.605  ops/s
[info] InternedStringsBenchMark.bruteForceDisk           1000000  thrpt   10       2.261 ±     0.524  ops/s
[info] InternedStringsBenchMark.diskBinarySearch            1000  thrpt   10   75739.802 ±  7761.782  ops/s
[info] InternedStringsBenchMark.diskBinarySearch           10000  thrpt   10   56250.447 ±  5006.917  ops/s
[info] InternedStringsBenchMark.diskBinarySearch          100000  thrpt   10   43687.978 ±  5832.726  ops/s
[info] InternedStringsBenchMark.diskBinarySearch         1000000  thrpt   10   32456.451 ±  2445.609  ops/s
[info] InternedStringsBenchMark.diskBtree                   1000  thrpt   10  302173.618 ± 18954.849  ops/s
[info] InternedStringsBenchMark.diskBtree                  10000  thrpt   10  253343.378 ±  9804.581  ops/s
[info] InternedStringsBenchMark.diskBtree                 100000  thrpt   10  171095.312 ±  9263.619  ops/s
[info] InternedStringsBenchMark.diskBtree                1000000  thrpt   10   94005.783 ±  9262.964  ops/s
[info] InternedStringsBenchMark.diskHash                    1000  thrpt   10  628110.101 ± 53130.348  ops/s
[info] InternedStringsBenchMark.diskHash                   10000  thrpt   10  552554.763 ± 57761.242  ops/s
[info] InternedStringsBenchMark.diskHash                  100000  thrpt   10  467621.735 ± 44194.539  ops/s
[info] InternedStringsBenchMark.diskHash                 1000000  thrpt   10  407083.315 ± 28498.544  ops/s
```

### Run benchmarks

Required:
- Java 11+
- Scala 2.13
- sbt 1.6.2

```bash
sbt jmh:run -i 10 -wi 3 -f1 -t1
```
More JMH options could be found here https://github.com/sbt/sbt-jmh
