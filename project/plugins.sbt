
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

resolvers ++= Seq(
  "AWV Snapshots" at "http://dev-colab.awv.vlaanderen.be/nexus/content/repositories/snapshots/",
  "AWV Releases" at "http://dev-colab.awv.vlaanderen.be/nexus/content/repositories/releases/"
)

addSbtPlugin("be.vlaanderen.awv" % "sbt-plugin" % "1.0.0")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.4")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")