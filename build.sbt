val scala3Version = "3.6.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "calendar",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,


    libraryDependencies ++= Seq(
      "org.scalafx" % "scalafx_3" % "22.0.0-R33",
      "com.sun.mail" % "javax.mail" % "1.6.2",
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    )


  )
