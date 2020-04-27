# scala2ts-sbt

This is the SBT-based plugin for the `scala2ts` project. Scala2TS is a Scala to Typescript transpiler that
takes a variety of Scala constructs and emits Typescript-compliant interfaces.

Currently handles:
* Case Classes
* Sealed Trait Hierarchies
* Generics
* Primitives
* Collections/Map
* java.time.*
* UUID

## Support

|**Version**|**SBT**|**Scala**|
|---|---|---|
|1.0.1|1.x|2.13<br />2.12<br />2.11|
|1.0.0|1.x|2.13<br />2.12<br />2.11|

## Usage

```sbt
// project/plugins.sbt

addSbtPlugin("com.github.scala2ts" % "scala2ts-sbt" % "1.0.1")
```

```sbt
// build.sbt

lazy val root = (project in file("."))
  .enablePlugins(Scala2TSPlugin)
  .settings(
    // Configuration options below
  )
```

No need to mess with running any SBT tasks, once you run `compile` the Typescript interfaces
are emitted to your `outDir`.

## Configuration

All configuration options are prefixed with `ts`.

|**Option**|**Default**|**Description**|
|---|---|---|
|Enable|false|Enable the plugin|
|Debug|false|Enable debug logging|
|IncludeFiles| |Files to include in compilation|
|ExcludeFiles| |Files to exclude in compilation|
|IncludeTypes| |Types/Packages to include in compilation| 
|ExcludeTypes| |Types/Packages to exclude in compilation|
|NamePrefix| |A prefix to use in TS interface names|
|NameSuffix| |A suffix to use in TS interface names|
|DateMapping|AsDate|How to map Scala Dates to TS|
|LongDoubleMapping|AsString|How to map Scala Longs and Doubles to TS|
|OutDir| |Where to output files created during compilation|
|OutFileName|index.d.ts|The name of the TS file to produce|
|PackageJsonName| |The name of the project in package.json (must be supplied to emit package.json)|
|PackageJsonVersion| |The version of the project in package.json|
|PackageJsonTypes| |The path to the types file in package.json|
|PackageJsonRegistry| |An external NPM registry url|

## Examples

### Include only certain packages to compile

A `Seq` of Regexes can be passed to `tsIncludeTypes` to include a certain set of packages
to be transpiled by `scala2ts`.

```sbt
// build.sbt

lazy val root = (project in file("."))
  .enablePlugins(Scala2TSPlugin)
  .settings(
    tsEnable := true,
    tsIncludeTypes := Seq(
      "com\\.some\\.package".r,
      "org\\.some\\.other\\.package".r
    )
  )
```

### Emit typescript to SBT's build directory

One can emit the transpiled typescript anywhere, but usually it's emitted to SBT's build directory.

```sbt
// build.sbt

lazy val root = (project in file("."))
  .enablePlugins(Scala2TSPlugin)
  .settings(
    tsEnable := true,
    tsIncludeTypes := Seq(
      "com\\.some\\.package".r,
      "org\\.some\\.other\\.package".r
    ),
    tsOutDir := {
      val targetPath = (target in Compile).value.getPath
    
      s"${targetPath}/typescript"
    } 
  )
```

### Include a package.json with the emitted Typescript

```sbt
// build.sbt

lazy val root = (project in file("."))
  .enablePlugins(Scala2TSPlugin)
  .settings(
    tsEnable := true,
    tsIncludeTypes := Seq(
      "com\\.some\\.package".r,
      "org\\.some\\.other\\.package".r
    ),
    tsOutDir := {
      val targetPath = (target in Compile).value.getPath
    
      s"${targetPath}/typescript"
    },
    tsPackageJsonName := "@yourcompany/types",
    tsPackageJsonVersion := version.value,
  )
```