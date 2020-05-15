package com.github.scala2ts

import sbt.{AutoPlugin, Def, _}
import sbt.Keys._

import scala.concurrent.duration._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.jse.SbtJsTask
import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.web.SbtWeb.autoImport._

import scala.util.matching.Regex
import com.github.scala2ts.configuration.Configuration.Args._
import com.github.scala2ts.configuration.DateMapping.DateMapping
import com.github.scala2ts.configuration.LongDoubleMapping.LongDoubleMapping

object Scala2TSPlugin extends AutoPlugin {
  private[this] val pluginName: String = "scala2ts"
  override def requires = SbtJsTask
  override def trigger = allRequirements

  object autoImport {
    val tsTranspile           = taskKey[Unit](
      "Transpile emitted typescript from scala2ts-core"
    )

    val tsEnable              = settingKey[Boolean](
      "Enable the Scala2TS Compiler Plugin and Compilation"
    )
    val tsDebug               = settingKey[Boolean](
      "Enable debug logging during compilation"
    )

    val tsOutputDirectory     = settingKey[String](
      "Path to the desired output directory"
    )

    val tsIncludeFiles        = settingKey[Seq[Regex]](
      "Files to include in compilation"
    )
    val tsExcludeFiles        = settingKey[Seq[Regex]](
      "Files to exclude in compilation"
    )
    val tsIncludeTypes        = settingKey[Seq[Regex]](
      "Types to include in compilation"
    )
    val tsExcludeTypes        = settingKey[Seq[Regex]](
      "Types to exclude in compilation"
    )

    val tsNamePrefix          = settingKey[String](
      "A prefix to use in type naming"
    )
    val tsNameSuffix          = settingKey[String](
      "A suffix to use in type naming"
    )

    val tsDateMapping         = settingKey[DateMapping](
      "How to transform Scala Date-types into Typescript"
    )
    val tsLongDoubleMapping   = settingKey[LongDoubleMapping](
      "How to transform Long(s) and Double(s) into Typescript"
    )

    val tsOutDir              = settingKey[String](
      "Directory path to emit Typescript file(s)"
    )
    val tsOutFileName         = settingKey[String](
      "What to name the resulting Typescript file"
    )

    val tsPackageJsonName     = settingKey[String](
      "The name to use in package.json"
    )
    val tsPackageJsonVersion  = settingKey[String](
      "The version to use in package.json"
    )
    val tsPackageJsonTypes    = settingKey[String](
      "The path to types to use in package.json"
    )
    val tsPackageJsonRegistry = settingKey[String](
      "The url to use for the publishConfig in package.json"
    )
  }

  import autoImport._

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    Seq(
      autoCompilerPlugins := true,
      addCompilerPlugin("com.github.scala2ts" %% "scala2ts-core" % "1.0.6"),
      JsEngineKeys.parallelism := 1,
      libraryDependencies ++= Seq(
        "org.webjars.npm" % "typescript" % "3.8.3"
      ),
      tsEnable := {
        tsEnable.??(false).value
      },
      tsDebug := {
        tsDebug.??(false).value
      },
      tsIncludeFiles := {
        tsIncludeFiles.??(Seq()).value
      },
      tsExcludeFiles := {
        tsExcludeFiles.??(Seq()).value
      },
      tsIncludeTypes := {
        tsIncludeTypes.??(Seq()).value
      },
      tsExcludeTypes := {
        tsExcludeTypes.??(Seq()).value
      },
      tsOutDir := s"${(target in Compile).value.getAbsolutePath}/typescript",
      tsOutFileName := {
        tsOutFileName.??("index.ts").value
      },
      tsTranspile := {
        SbtJsTask.executeJs(
          state.value,
          JsEngineKeys.engineType.value,
          None,
          (nodeModuleDirectories in Plugin).value.map(_.getPath),
          ((nodeModuleDirectories in Plugin).value / "typescript" / "lib" / "tsc.js").get.head,
          Seq(
            "--declaration",
            "true",
            "--listEmittedFiles",
            "true",
            s"${tsOutDir.value}/${tsOutFileName.value}"
          ),
          30 seconds,
        )
        streams.value.log("Deleting original .ts file")
        new File(s"${tsOutDir.value}/${tsOutFileName.value}").delete()
      }
    ) ++ inConfig(Compile)(Seq(
      scalacOptions ++= {
        if (tsEnable.value) {
          val debugArgs: Seq[String] = transformArg[Boolean](
            debugArg,
            tsDebug.?.value: @sbtUnchecked,
            b => b.toString
          )

          val includeFilesArgs: Seq[String] = transformArgs[Regex](
            fileIncludesArg,
            tsIncludeFiles.value: @sbtUnchecked,
            regex => regex.toString
          )
          val excludeFilesArgs: Seq[String] = transformArgs[Regex](
            fileExcludesArg,
            tsExcludeFiles.value: @sbtUnchecked,
            regex => regex.toString
          )
          val includeTypesArgs: Seq[String] = transformArgs[Regex](
            typeIncludesArg,
            tsIncludeTypes.value: @sbtUnchecked,
            regex => regex.toString
          )
          val excludeTypesArgs: Seq[String] = transformArgs[Regex](
            typeExcludesArg,
            tsExcludeTypes.value: @sbtUnchecked,
            regex => regex.toString
          )

          val typeNamePrefixArgs: Seq[String] = transformArg[String](
            typeNamePrefixArg,
            tsNamePrefix.?.value: @sbtUnchecked,
            identity
          )

          val typeNameSuffixArgs: Seq[String] = transformArg[String](
            typeNameSuffixArg,
            tsNameSuffix.?.value: @sbtUnchecked,
            identity
          )

          val dateMappingArgs: Seq[String] = transformArg[DateMapping](
            dateMappingArg,
            tsDateMapping.?.value: @sbtUnchecked,
            arg => s"${arg.id}",
          )

          val longDoubleMappingArgs: Seq[String] = transformArg[LongDoubleMapping](
            longDoubleMappingArg,
            tsLongDoubleMapping.?.value: @sbtUnchecked,
            arg => s"${arg.id}"
          )

          val outDirArgs: Seq[String] = transformArg[String](
            outDirArg,
            tsOutDir.?.value.map(_.toString): @sbtUnchecked,
            identity
          )

          val outFileNameArgs: Seq[String] = transformArg[String](
            outFileNameArg,
            tsOutFileName.?.value: @sbtUnchecked,
            identity
          )

          val packageJsonNameArgs: Seq[String] = transformArg[String](
            packageJsonNameArg,
            tsPackageJsonName.?.value: @sbtUnchecked,
            identity
          )
          val packageJsonVersionArgs: Seq[String] = transformArg[String](
            packageJsonVersionArg,
            tsPackageJsonVersion.?.value: @sbtUnchecked,
            identity
          )
          val packageJsonTypesArgs: Seq[String] = transformArg[String](
            packageJsonTypesArg,
            tsPackageJsonTypes.?.value: @sbtUnchecked,
            identity
          )
          val packageJsonRegistryArgs: Seq[String] = transformArg[String](
            packageJsonRegistryArg,
            tsPackageJsonRegistry.?.value: @sbtUnchecked,
            identity
          )

          debugArgs ++
          includeFilesArgs ++
          excludeFilesArgs ++
          includeTypesArgs ++
          excludeTypesArgs ++
          typeNamePrefixArgs ++
          typeNameSuffixArgs ++
          dateMappingArgs ++
          longDoubleMappingArgs ++
          outDirArgs ++
          outFileNameArgs ++
          packageJsonNameArgs ++
          packageJsonVersionArgs ++
          packageJsonTypesArgs ++
          packageJsonRegistryArgs
        } else {
          Seq(s"-Xplugin-disable:$pluginName")
        }
      }
    ))

  private[this] def transformArg[T](namespace: String, arg: Option[T], fn: T => String): Seq[String] =
    arg match {
      case Some(value) => Seq(s"-P:$pluginName:$namespace${fn(value)}")
      case None => Seq.empty
    }

  /**
   * Transform the option into the correct compiler plugin command line string
   * Option namespaces are delimited by ":" which are controlled by the core library
   */
  private[this] def transformArgs[T](namespace: String, args: Seq[T], fn: T => String): Seq[String] =
    args.map(arg => s"-P:$pluginName:$namespace${fn(arg)}")
}
