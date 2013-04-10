// Comment this to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Play framework integration
// TODO CLI should set correct Play version here based on user's installed Play version
addSbtPlugin("play" % "sbt-plugin" % "2.1.0")