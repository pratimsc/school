name := "attend01"

version := "0.1"

lazy val `attend01` = (project in file(".")).enablePlugins(PlayScala)

//enablePlugins(ScalaJSPlugin)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(jdbc, cache, ws, specs2 % Test,
  "joda-time" % "joda-time" % "2.9.1",
  "org.joda" % "joda-money" % "0.10.0",
  "com.typesafe.play" %% "anorm" % "2.5.0"
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  