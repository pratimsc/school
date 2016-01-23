name := "attend01"

version := "0.1"

lazy val `attend01` = (project in file(".")).enablePlugins(PlayScala)

//enablePlugins(ScalaJSPlugin)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(jdbc, cache, ws, filters, specs2 % Test,
  "joda-time" % "joda-time" % "2.9.1",
  "org.joda" % "joda-money" % "0.10.0"
  //"com.typesafe.play" %% "anorm" % "2.5.0",
  //"com.orientechnologies" % "orientdb-core" % "2.1.9",
  //"org.allenai.tinkerpop.blueprints" % "blueprints-core" % "2.7.1",
  //"com.orientechnologies" % "orientdb-graphdb" % "2.1.9" intransitive(),
  // "com.orientechnologies" % "orientdb-client" % "2.1.9",
  //"com.orientechnologies" % "orientdb-enterprise" % "2.1.9"
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  