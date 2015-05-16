val sparkCore = "org.apache.spark" %% "spark-core" % "1.3.1"
val sparkStreaming = "org.apache.spark" %% "spark-streaming" % "1.3.1"
val sparkTwitter = "org.apache.spark" %% "spark-streaming-twitter" % "1.3.1"
val scopt = "com.github.scopt" %% "scopt" % "3.3.0"

lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.6"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "Twitter Panic",
    libraryDependencies += sparkCore,
    libraryDependencies += sparkStreaming,
    libraryDependencies += sparkTwitter,
    libraryDependencies += scopt,
    resolvers += Resolver.sonatypeRepo("public")
  )
