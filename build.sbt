import de.heikoseeberger.sbtheader.license.License

// project info
organization := "com.github.everpeace"
name         := "banditsbook-scala"
version      := "0.1.0"

// compile settingss
scalaVersion       :=  "2.11.8"
crossScalaVersions :=  Seq("2.10.6", "2.11.8")
scalacOptions      ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yinline-warnings",
    "-Ywarn-dead-code",
    "-Xfuture"
)

// dependencies
resolvers           ++= Seq(
    "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)
libraryDependencies ++= Seq(
    "org.scalanlp" %% "breeze" % "0.12",
    "org.scalanlp" %% "breeze-natives" % "0.12",
    "org.scalanlp" %% "breeze-viz" % "0.12",
    "org.typelevel" %% "cats" % "0.5.0",
    "com.typesafe" % "config" % "1.3.0",
    "org.scala-lang.modules" %% "scala-pickling" % "0.10.1",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.0" % "test"
)

// auto import setting for "sbt console"
initialCommands := "import com.github.everpeace.banditsbook._"

// sbt-headers plugin settings.
unmanagedSourceDirectories in Compile += baseDirectory.value / "plot"
headers := Map(
    "scala" -> MIT("2016", "Shingo Omura"),
    "conf" -> MIT("2016", "Shingo Omura", "#"),
    "r" -> MIT("2016", "Shingo Omura", "#"),
    "sh" -> MIT("2016", "Shingo Omura", "#")
)
