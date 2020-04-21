/**
 * scala2ts-sbt SBT build file
 */

import ReleaseTransformations._

lazy val root = (project in file("."))
  .settings(
    name := "scala2ts-sbt",
    organization := "com.github.scala2ts",
    scalaVersion := "2.12.11",
    sbtPlugin := true,
    crossVersion := CrossVersion.binary,
    crossScalaVersions := Seq(
      // TODO: "2.10.7"
      scalaVersion.value
    ),
    sbtVersion in pluginCrossBuild := {
      scalaBinaryVersion.value match {
        case "2.10" => "0.13.18"
        case "2.12" => "1.3.10"
      }
    },
    libraryDependencies ++= Seq(
      "com.github.scala2ts" %% "scala2ts-core" % "1.0.0-SNAPSHOT"
    ),
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    ),
    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomExtra :=
      <url>https://github.com/scala2ts/scala2ts-sbt</url>
        <licenses>
          <license>
            <name>MIT</name>
            <url>https://opensource.org/licenses/MIT</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:scala2ts/scala2ts-sbt.git</url>
          <connection>scm:git:git@github.com:scala2ts/scala2ts-sbt.git</connection>
        </scm>
        <developers>
          <developer>
            <id>halfmatthalfcat</id>
            <name>Matt Oliver</name>
            <url>https://github.com/halfmatthalfcat</url>
          </developer>
        </developers>
  )
