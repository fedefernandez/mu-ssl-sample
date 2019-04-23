import sbt._
import sbt.Keys._

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val scalaV = "2.12.8"

    lazy val protocolSettings: Seq[Def.Setting[_]] = Nil

    lazy val serverSettings: Seq[Def.Setting[_]] =
      libraryDependencies ++= Seq(
        "io.higherkindness" %% "mu-rpc-server" % "0.17.2",
        "io.higherkindness" %% "mu-rpc-netty" % "0.17.2",
        "io.higherkindness" %% "mu-rpc-netty-ssl" % "0.17.2"
      )

    lazy val clientSettings: Seq[Def.Setting[_]] =
      libraryDependencies ++= Seq(
        "io.higherkindness" %% "mu-rpc-netty" % "0.17.2",
        "io.higherkindness" %% "mu-rpc-netty-ssl" % "0.17.2"
      )
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      scalaVersion := scalaV,
      name := "sample-ssl",
      organization := "io.higherkindness",
      resolvers ++= Seq(Resolver.sonatypeRepo("snapshots")),
      scalacOptions += "-Xplugin-require:macroparadise",
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
      libraryDependencies ++= Seq(
        "com.chuusai" %% "shapeless" % "2.3.3",
        "io.higherkindness" %% "mu-rpc-channel" % "0.17.2",
	      "org.slf4j" % "slf4j-simple" % "1.7.26"
      )
    )

}
