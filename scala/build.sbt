val sparkCore = "org.apache.spark" %% "spark-core" % "1.4.0" % "provided"
val sparkStreaming = "org.apache.spark" %% "spark-streaming" % "1.4.0" % "provided"
val sparkTwitter = "org.apache.spark" %% "spark-streaming-twitter" % "1.4.0" % "provided"
val scopt = "com.github.scopt" %% "scopt" % "3.3.0"

lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.10.5"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "TwitterPanic",
    libraryDependencies ++= Seq(sparkCore,
      sparkStreaming,
      sparkTwitter,
      scopt),
    resolvers += Resolver.sonatypeRepo("public")
  )
