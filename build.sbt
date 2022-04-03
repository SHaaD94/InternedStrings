ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "InternedStrings"
  )



libraryDependencies += "com.koloboke" % "koloboke-api-jdk8" % "1.0.0"
libraryDependencies += "com.koloboke" % "koloboke-impl-jdk8" % "1.0.0"
libraryDependencies += "com.koloboke" % "koloboke-compile" % "0.5.1"

libraryDependencies += "org.openjdk.jmh" % "jmh-core" % "1.34"
libraryDependencies += "org.openjdk.jmh" % "jmh-generator-annprocess" % "1.34"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"