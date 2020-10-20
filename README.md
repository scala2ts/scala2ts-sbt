# scala2ts-sbt

This is the SBT-based plugin for the `scala2ts` project. Scala2TS is a Scala to Typescript transpiler that
takes a variety of Scala constructs and emits Typescript-compliant code.

Currently handles:
* Case Classes
* Sealed Trait Hierarchies
* General Traits
* Generics
* Primitives
* Collections/Map
* java.time.*
* UUID
* Enums (Scala and Enumeratum's `EnumEntry`)
  * Only supporting plain Enumeratum sealed trait enums without any name-trait mixins.

## Support

|**Version**|**SBT**|**Scala**|
|---|---|---|
|1.1.x|1.x|2.13<br />2.12<br />2.11|
|1.0.x|1.x|2.13<br />2.12<br />2.11|

## Usage

```sbt
// project/plugins.sbt

addSbtPlugin("com.github.scala2ts" % "scala2ts-sbt" % "1.1.0")
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

## Transpiling The Emitted Typescript (tsTranspile)

The `scala2ts-core` compiler plugin emits a Typescript implementation file (`.ts`) versus a declaration file (`.d.ts`).

A declaration file can be consumed by any Typescript project with relative ease while consuming an implementation file,
usually through an npm module, causes issues when compiling your project. This is because most frontend build tooling
expects npm modules to be transplied and in vanilla Javascript (unless you tune your bundlers correctly).

This plugin ships with the ability to transpile the emitted implementation file using the shipped WebJar typescript
compiler using the `tsTranspile` task. You shouldn't need to have `tsc` or `node` installed locally for this to work
as it leverages the `sbt-js-engine` plugin (and will fallback to the Trireme node runtime if `node` is not found locally)

This task produces a transpiled `.js` file as well as a `.d.ts` declaration file that is then safe to publish or consume
locally in any Typescript or Javascript project.

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
|SealedTypesMapping|None|Include an enum or type union of sealed traits/classes's members|
|IncludeClassDefinition|false|Include class definitions based on built interfaces|
|IncludeDiscriminator|false|Include a class property that acts as a discriminator|
|DiscriminatorName|"type"|The name of the class discriminator property|
|OutDir|target/typescript|Where to output files created during compilation|
|OutFileName|index.ts|The name of the TS file to produce|
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
