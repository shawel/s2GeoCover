resolvers += "jitpack" at "https://jitpack.io"

name := "sparks2"

version := "0.0.1"

scalaVersion := "2.11.12"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.7"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.2.2"
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.2.2"
//libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.2.2"
libraryDependencies += "com.github.davidmoten" % "geo" % "0.7.1"
libraryDependencies += "io.sgr" % "s2-geometry-library-java" % "1.0.0"
libraryDependencies += "org.locationtech.jts" % "jts-core" % "1.16.0"
libraryDependencies += "org.locationtech.jts" % "jts-io" % "1.16.0"
libraryDependencies += "org.locationtech.jts.io" % "jts-io-common" % "1.16.0"

libraryDependencies += "org.wololo" % "jts2geojson" % "0.13.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

fork in Test := true

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

// JAR file settings
//assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
// Add the JAR file naming conventions described here: https://github.com/MrPowers/spark-style-guide#jar-files
// You can add the JAR file naming conventions by running the shell script
