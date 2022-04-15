# Interned Strings

Comparison of different implementations of `string->int` dictionaries

### Benchmark results
```
[info] Benchmark                                            (elementsInBucket)  (stringsCount)   Mode  Cnt       Score       Error  Units
[info] InternedStringsBenchMark.bruteForceDisk                             N/A            1000  thrpt   10    2357.745 ±   228.551  ops/s
[info] InternedStringsBenchMark.bruteForceDisk                             N/A           10000  thrpt   10     246.889 ±    10.628  ops/s
[info] InternedStringsBenchMark.bruteForceDisk                             N/A          100000  thrpt   10      25.218 ±     2.250  ops/s
[info] InternedStringsBenchMark.bruteForceDisk                             N/A         1000000  thrpt   10       2.321 ±     0.400  ops/s
[info] InternedStringsBenchMark.levelDB                                    N/A            1000  thrpt   10  1492640.669 ± 2455.066  ops/s
[info] InternedStringsBenchMark.levelDB                                    N/A           10000  thrpt   10  1062254.492 ± 1549.465  ops/s
[info] InternedStringsBenchMark.levelDB                                    N/A          100000  thrpt   10   473684.436 ± 1107.087  ops/s
[info] InternedStringsBenchMark.levelDB                                    N/A         1000000  thrpt   10   176597.247 ±  237.621  ops/s
[info] InternedStringsBenchMark.diskBinarySearch                           N/A            1000  thrpt   10   79921.973 ±  7606.416  ops/s
[info] InternedStringsBenchMark.diskBinarySearch                           N/A           10000  thrpt   10   55467.452 ±  2670.391  ops/s
[info] InternedStringsBenchMark.diskBinarySearch                           N/A          100000  thrpt   10   43477.914 ±  8069.832  ops/s
[info] InternedStringsBenchMark.diskBinarySearch                           N/A         1000000  thrpt   10   34417.994 ±  3414.570  ops/s
[info] InternedStringsBenchMark.diskBtree                                  N/A            1000  thrpt   10  284517.512 ± 41600.752  ops/s
[info] InternedStringsBenchMark.diskBtree                                  N/A           10000  thrpt   10  269594.859 ± 28825.275  ops/s
[info] InternedStringsBenchMark.diskBtree                                  N/A          100000  thrpt   10  179526.221 ± 13063.511  ops/s
[info] InternedStringsBenchMark.diskBtree                                  N/A         1000000  thrpt   10   97925.830 ± 12024.454  ops/s
[info] InternedStringsBenchMark.diskHash                                   N/A            1000  thrpt   10  612879.772 ± 50044.359  ops/s
[info] InternedStringsBenchMark.diskHash                                   N/A           10000  thrpt   10  560336.455 ± 64041.861  ops/s
[info] InternedStringsBenchMark.diskHash                                   N/A          100000  thrpt   10  489856.353 ± 46924.787  ops/s
[info] InternedStringsBenchMark.diskHash                                   N/A         1000000  thrpt   10  422422.872 ± 24973.626  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            10            1000  thrpt   10  496660.644 ± 92513.455  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            10           10000  thrpt   10  495358.632 ± 71035.219  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            10          100000  thrpt   10  438734.990 ± 81352.096  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            10         1000000  thrpt   10  324338.023 ± 21096.863  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            20            1000  thrpt   10  472823.718 ± 62610.982  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            20           10000  thrpt   10  442191.481 ± 33003.307  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            20          100000  thrpt   10  361252.750 ± 26319.311  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            20         1000000  thrpt   10  257928.693 ± 11974.887  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            50            1000  thrpt   10  373867.962 ± 26178.719  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            50           10000  thrpt   10  343218.612 ± 27014.275  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            50          100000  thrpt   10  297475.657 ±  2738.448  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                            50         1000000  thrpt   10  197371.191 ± 13525.148  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                           100            1000  thrpt   10  268694.177 ± 20529.348  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                           100           10000  thrpt   10  294420.792 ± 30184.084  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                           100          100000  thrpt   10  260983.301 ± 50846.423  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                           100         1000000  thrpt   10  143000.698 ± 14065.059  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                           200            1000  thrpt   10  167275.420 ± 19716.553  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                           200           10000  thrpt   10  157552.069 ± 12695.005  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                           200          100000  thrpt   10  138340.431 ±  8077.767  ops/s
[info] InternedStringsBenchMark.diskHashBucketed                           200         1000000  thrpt   10   90639.456 ±  8446.622  ops/s```
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
