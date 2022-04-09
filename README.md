# Interned Strings

Comparison of different approaches to organize `string->int` dictionaries

### Benchmark results
```
[info] InternedStringsBenchMark.diskBinarySearch  thrpt    10  0.469 ± 0.151  ops/s
[info] InternedStringsBenchMark.diskBtree         thrpt    10  1.942 ± 0.940  ops/s
[info] InternedStringsBenchMark.diskHash          thrpt    10  5.716 ± 1.621  ops/s
[info] InternedStringsBenchMark.diskBinarySearch   avgt    10  2.204 ± 0.241   s/op
[info] InternedStringsBenchMark.diskBtree          avgt    10  0.534 ± 0.087   s/op
[info] InternedStringsBenchMark.diskHash           avgt    10  0.187 ± 0.054   s/op
```

### Running benchmarks
```bash
sbt jmh:run -i 10 -wi 3 -f1 -t1 
```
More JMH options could be found here https://github.com/sbt/sbt-jmh
