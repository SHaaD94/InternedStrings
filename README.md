# Interned Strings

Comparison of different implementations of `string->int` dictionaries

### Benchmarks
1. All implementations with 1000 strings
```bash
sbt jmh:run -i 10 -wi 3 -f1 -t1 -p stringsCount=1000
```
Results:
```
[info] Benchmark                                  (stringsCount)   Mode  Cnt    Score    Error  Units
[info] InternedStringsBenchMark.bruteForceDisk              1000  thrpt   10    1.956 ±  0.230  ops/s
[info] InternedStringsBenchMark.diskBinarySearch            1000  thrpt   10   63.498 ± 18.761  ops/s
[info] InternedStringsBenchMark.diskBtree                   1000  thrpt   10  308.429 ± 43.454  ops/s
[info] InternedStringsBenchMark.diskHash                    1000  thrpt   10  618.475 ± 42.171  ops/s
[info] InternedStringsBenchMark.bruteForceDisk              1000   avgt   10    0.549 ±  0.128   s/op
[info] InternedStringsBenchMark.diskBinarySearch            1000   avgt   10    0.014 ±  0.003   s/op
[info] InternedStringsBenchMark.diskBtree                   1000   avgt   10    0.004 ±  0.001   s/op
[info] InternedStringsBenchMark.diskHash                    1000   avgt   10    0.002 ±  0.001   s/op
```
2. All implementations except bruteforce
```bash
sbt jmh:run -i 10 -wi 3 -f1 -t1 -e brute.*
```
Results:
```
[info] InternedStringsBenchMark.diskBinarySearch  thrpt    10  0.469 ± 0.151  ops/s
[info] InternedStringsBenchMark.diskBtree         thrpt    10  1.942 ± 0.940  ops/s
[info] InternedStringsBenchMark.diskHash          thrpt    10  5.716 ± 1.621  ops/s
[info] InternedStringsBenchMark.diskBinarySearch   avgt    10  2.204 ± 0.241   s/op
[info] InternedStringsBenchMark.diskBtree          avgt    10  0.534 ± 0.087   s/op
[info] InternedStringsBenchMark.diskHash           avgt    10  0.187 ± 0.054   s/op
```
More JMH options could be found here https://github.com/sbt/sbt-jmh
