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

    val outputDirectory       = settingKey[String](
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

    val indentString          = settingKey[String](
      "Indent string (tab/space) to use in the output file(s)"
    )
    val typeNamePrefix        = settingKey[String](
      "A prefix to use in type naming"
    )
    val typeNameSuffix        = settingKey[String](
      "A suffix to use in type naming"
    )

    val emitInterfaces        = settingKey[Boolean](
      "Should emit Typescript interfaces"
    )
    val emitClasses           = settingKey[Boolean](
      "Should emit Typescript classes"
    )

    val optionToNullable      = settingKey[Boolean](
      "Convert Option types to union with null"
    )
    val optionToUndefined     = settingKey[Boolean](
      "Convert Option types to union with undefined"
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

          val indentStringArgs: Seq[String] = transformArg[String](
            indentStringArg,
            indentString.?.value,
            identity
          )

          val typeNamePrefixArgs: Seq[String] = transformArg[String](
            typeNamePrefixArg,
            typeNamePrefix.?.value,
            identity
          )

          val typeNameSuffixArgs: Seq[String] = transformArg[String](
            typeNameSuffixArg,
            typeNameSuffix.?.value,
            identity
          )

          val emitInterfacesArgs: Seq[String] = transformArg[Boolean](
            emitInterfacesArg,
            emitInterfaces.?.value,
            b => s"$b"
          )

          val emitClassesArgs: Seq[String] = transformArg[Boolean](
            emitClassesArg,
            emitClasses.?.value,
            b => s"$b"
          )

          val optionToNullableArgs: Seq[String] = transformArg[Boolean](
            optionToNullableArg,
            optionToNullable.?.value,
            b => s"$b"
          )

          val optionToUndefinedArgs: Seq[String] = transformArg[Boolean](
            optionToUndefinedArg,
            optionToUndefined.?.value,
            b => s"$b"
          )

          includeFilesArgs ++
          excludeFilesArgs ++
          includeTypesArgs ++
          excludeTypesArgs ++
          indentStringArgs ++
          typeNamePrefixArgs ++
          typeNameSuffixArgs ++
          emitInterfacesArgs ++
          emitClassesArgs ++
          optionToNullableArgs ++
          optionToUndefinedArgs
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
