package com.github.scala2ts

import sbt.AutoPlugin
import sbt.Keys._
import sbt.{Def, _}

import scala.util.matching.Regex

import com.github.scala2ts.configuration.Configuration.Args._

object Scala2TSPlugin extends AutoPlugin {
  private[this] val pluginName: String = "scala2ts"

  object autoImport {
    val enableScala2TS        = settingKey[Boolean](
      "Enable the Scala2TS Compiler Plugin and Compilation"
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
  }

  import autoImport._

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    Seq(
      autoCompilerPlugins := true,
      addCompilerPlugin("com.github.scala2ts" %% "scala2ts-core" % "1.0.0-SNAPSHOT"),
    ) ++ inConfig(Compile)(Seq(
      enableScala2TS := {
        enableScala2TS.??(false).value
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

      scalacOptions ++= {
        if (enableScala2TS.value) {
          val includeFilesArgs: Seq[String] = transformArgs[Regex](
            fileIncludesArg,
            tsIncludeFiles.value,
            regex => regex.toString
          )
          val excludeFilesArgs: Seq[String] = transformArgs[Regex](
            fileExcludesArg,
            tsExcludeFiles.value,
            regex => regex.toString
          )
          val includeTypesArgs: Seq[String] = transformArgs[Regex](
            typeIncludesArg,
            tsIncludeTypes.value,
            regex => regex.toString
          )
          val excludeTypesArgs: Seq[String] = transformArgs[Regex](
            typeExcludesArg,
            tsExcludeTypes.value,
            regex => regex.toString
          )

          includeFilesArgs ++
          excludeFilesArgs ++
          includeTypesArgs ++
          excludeTypesArgs
        } else {
          Seq(s"-Xplugin-disable:$pluginName")
        }
      }
    ))


  /**
   * Transform the option into the correct compiler plugin command line string
   * Option namespaces are delimited by ":" which are controlled by the core library
   */
  private[this] def transformArgs[T](namespace: String, args: Seq[T], fn: T => String): Seq[String] =
    args.map(arg => s"-P:$pluginName:$namespace${fn(arg)}")
}
