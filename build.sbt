ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "scalac_task",
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Dependencies.live,
    addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full)
  )
