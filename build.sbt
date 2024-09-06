ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.2"

lazy val root = (project in file("."))
  .settings(
    name := "spendless"
  )

val PekkoVersion          = "1.0.3"
val PekkoHttpVersion      = "1.0.1"
val LogBackVersion        = "1.5.6"
val CatsEffectVersion     = "3.5.4"
val PostgresVersion       = "42.7.3"
val SlickVersion          = "3.5.1"
val SlickPGVersion        = "0.22.2"
val SlickEffectVersion    = "0.6.0"
val MacWireVersion        = "2.5.9"
val SealedMonadVersion    = "1.3.0"
val CirceVersion          = "0.14.9"
val PekkoHttpJsonVersion  = "2.6.0"
val Log4CatsVersion       = "2.7.0"

libraryDependencies ++= Seq(
  "org.apache.pekko"              %% "pekko-actor-typed"   % PekkoVersion,
  "org.apache.pekko"              %% "pekko-stream"        % PekkoVersion,
  "org.apache.pekko"              %% "pekko-http"          % PekkoHttpVersion,
  "ch.qos.logback"                %  "logback-classic"     % LogBackVersion,
  "org.typelevel"                 %% "cats-effect"         % CatsEffectVersion,
  "org.postgresql"                %  "postgresql"          % PostgresVersion,
  "com.typesafe.slick"            %% "slick"               % SlickVersion,
  "com.typesafe.slick"            %% "slick-hikaricp"      % SlickVersion,
  "com.github.tminglei"           %% "slick-pg"            % SlickPGVersion,
  "com.github.tminglei"           %% "slick-pg_circe-json" % SlickPGVersion,
  "com.softwaremill.macwire"      %% "macros"              % MacWireVersion % "provided",
  "pl.iterators"                  %% "sealed-monad"        % SealedMonadVersion,
  "io.circe"                      %% "circe-core"          % CirceVersion,
  "io.circe"                      %% "circe-parser"        % CirceVersion,
  "io.circe"                      %% "circe-generic"       % CirceVersion,
  "com.github.pjfanning"          %% "pekko-http-circe"    % PekkoHttpJsonVersion,
  "org.typelevel"                 %% "log4cats-slf4j"       % Log4CatsVersion

)
