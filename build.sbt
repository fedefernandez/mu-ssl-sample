lazy val `sample-ssl-protocol` = project
  .in(file("protocol"))
  .settings(name := "sample-ssl-protocol")
  .settings(protocolSettings)

lazy val `sample-ssl-server` = project
  .in(file("server"))
  .settings(name := "sample-ssl-server")
  .settings(serverSettings)
  .dependsOn(`sample-ssl-protocol`)

lazy val `sample-ssl-client` = project
  .in(file("client"))
  .settings(name := "sample-ssl-client")
  .settings(clientSettings)
  .dependsOn(`sample-ssl-protocol`)

lazy val `root` = project
  .in(file("."))
  .aggregate(allModules: _*)
  .dependsOn(allModulesDeps: _*)


lazy val allModules: Seq[ProjectReference] = Seq(
  `sample-ssl-server`,
  `sample-ssl-client`,
  `sample-ssl-protocol`
)

lazy val allModulesDeps: Seq[ClasspathDependency] =
  allModules.map(ClasspathDependency(_, None))