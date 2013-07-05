import AssemblyKeys._

assemblySettings

libraryDependencies ++= Seq(
  "org.fusesource.scalate" % "scalate-core" % "1.5.3",
  "org.slf4j" % "slf4j-api" % "1.7.0",
  "com.dadrox" % "quiet-slf4j" % "0.1"
)

name := "difr"

version := "0.1-SNAPSHOT"

mainClass in assembly := Some("nl.flotsam.difr.Tool")