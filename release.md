# Release Procedure

## General

Atomium artefacts are published to [Sonatype OSS](https://oss.sonatype.org/), which 
sync's with Maven Central repositories.

We use the [sbt-sonatype plugin](https://github.com/xerial/sbt-sonatype). The procudure is:

~~~
$ sbt  +test # tests the code
$ sbt +publishSigned  #publishes the signed artefacts to staging
$ sbt release

~~~~