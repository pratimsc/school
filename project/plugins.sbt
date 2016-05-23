logLevel := Level.Warn

resolvers += "Typesafe repository 1" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe repository 2" at "https://dl.bintray.com/typesafe/maven-releases/"


addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.3")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.5")