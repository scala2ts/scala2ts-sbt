package com.github.scala2ts

import sbt.AutoPlugin
import sbt.Keys._
import sbt.{Def, _}

import scala.util.matching.Regex

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
    inConfig(Compile)(Seq(
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
            "file:includes",
            tsIncludeFiles.value,
            regex => regex.toString
          )
          val excludeFilesArgs: Seq[String] = transformArgs[Regex](
            "file:excludes",
            tsExcludeFiles.value,
            regex => regex.toString
          )
          val includeTypesArgs: Seq[String] = transformArgs[Regex](
            "type:includes",
            tsIncludeTypes.value,
            regex => regex.toString
          )
          val excludeTypesArgs: Seq[String] = transformArgs[Regex](
            "type:excludes",
            tsExcludeTypes.value,
            regex => regex.toString
          )

          includeFilesArgs ++
          excludeFilesArgs ++
          includeTypesArgs ++
          excludeTypesArgs
        } else { Seq.empty }
      }
    ))

  private[this] def transformArgs[T](namespace: String, args: Seq[T], fn: T => String): Seq[String] =
    args.map(_ => s"-P:$pluginName:$namespace:${fn(_)}")
}
