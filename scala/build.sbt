val sparkCore = "org.apache.spark" %% "spark-core" % "1.4.0" % "provided"
val sparkStreaming = "org.apache.spark" %% "spark-streaming" % "1.4.0" % "provided"
val sparkTwitter = "org.apache.spark" %% "spark-streaming-twitter" % "1.4.0"
val scopt = "com.github.scopt" %% "scopt" % "3.3.0"

lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.10.5"
)

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", xs @ _*)   => MergeStrategy.first
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate
    }
  case _ => MergeStrategy.deduplicate
}

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
