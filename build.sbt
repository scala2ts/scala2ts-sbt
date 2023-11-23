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
      "com.github.scala2ts" %% "scala2ts-core" % "1.1.3"
    ),
    releaseCrossBuild := true,
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
    pomExtra :=
      <url>https://www.github.com/scala2ts/scala2ts-sbt</url>
        <licenses>
          <license>
            <name>MIT</name>
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
            <url>https://www.github.com/halfmatthalfcat</url>
          </developer>
        </developers>,
    publishMavenStyle := true,
    publishTo := sonatypePublishToBundle.value,
    resolvers ++= Seq(DefaultMavenRepository)
  )

/**
 * So I guess plugins added to the root of the project's build.sbt
 * are inherited by the consuming project? Works for me...
 */
addSbtPlugin("com.github.sbt" % "sbt-js-engine" % "1.3.5")