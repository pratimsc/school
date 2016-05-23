name := "attend01"

version := "0.1"

lazy val `attend01` = (project in file(".")).enablePlugins(PlayScala)

//enablePlugins(ScalaJSPlugin)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(jdbc, cache, ws, filters, specs2 % Test,
  "joda-time" % "joda-time" % "2.9.3",
  "org.joda" % "joda-money" % "0.11",
  //"com.typesafe.play" %% "anorm" % "2.5.0",
  //"com.orientechnologies" % "orientdb-core" % "2.1.9",
  //"org.allenai.tinkerpop.blueprints" % "blueprints-core" % "2.7.1",
  //"com.orientechnologies" % "orientdb-graphdb" % "2.1.9" intransitive(),
  // "com.orientechnologies" % "orientdb-client" % "2.1.9",
  //"com.orientechnologies" % "orientdb-enterprise" % "2.1.9"
  "com.mohiva" %% "play-silhouette" % "4.0.0-BETA4",
    "com.mohiva" %% "play-silhouette-testkit" % "4.0.0-BETA4" % "test",
  "be.objectify" %% "deadbolt-scala" % "2.5.0"
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers ++= Seq(
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
)