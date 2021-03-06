import sbt.Keys._ //{libraryDependencies, version}

ThisBuild / organization := "fr.telecom"
ThisBuild / scalaVersion := "2.11.11"

lazy val root = (project in file(".")).
  settings(
    name := "GDELT-Explore",
    version := "0.1.0",
    publishMavenStyle := true
  )

val SparkVersion = "2.4.4"
val SparkCompatibleVersion = "2.4"
val HadoopVersion = "2.7.2"

val AwsVersion = "1.11.703"

val CassandraVersion = "3.11.5"
val SparkCassandraVersion = "2.4.2"

// Raw spark dependencies to be used either as "provided" or "compiled"
lazy val sparkDependencies = Seq(
  "org.apache.spark" %% "spark-core" % SparkVersion,
  "org.apache.spark" %% "spark-sql" % SparkVersion,
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % HadoopVersion,
  "org.apache.hadoop" % "hadoop-common" % HadoopVersion
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-bom" % AwsVersion,
  "com.amazonaws" % "aws-java-sdk-s3" % AwsVersion,
  "com.datastax.spark" %% "spark-cassandra-connector" % SparkCassandraVersion,
  "log4j" % "log4j" % "1.2.17"
)

// When building assembly to be submitted to Spark, do not include the Spark libs in the Jar
libraryDependencies ++= sparkDependencies.map(_ % "provided")

// Configuration to be used in debug from IntelliJ, must include the Spark Jars
// Must select this in the run configuration on IntelliJ as parameter "Use classpath of module: mainRunner"
// https://github.com/JetBrains/intellij-scala/wiki/%5BSBT%5D-How-to-use-provided-libraries-in-run-configurations
lazy val mainRunner = project.in(file("mainRunner")).dependsOn(RootProject(file("."))).settings(
  libraryDependencies ++= sparkDependencies.map(_ % "compile")
)

// Disable parallel execution because of spark-testing-base
Test / parallelExecution := false

// A special option to exclude Scala itself form our assembly JAR, since Spark-submit or IntelliJ already bundle Scala.
assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)

// Configure the build to publish the assembly JAR
(Compile / assembly / artifact) := {
  val art = (Compile / assembly / artifact).value
  art.withClassifier(Some("assembly"))
}

addArtifact(Compile / assembly / artifact, assembly)