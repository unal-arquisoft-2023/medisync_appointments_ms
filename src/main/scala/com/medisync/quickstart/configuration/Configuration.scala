package com.medisync.quickstart

import com.typesafe.config.ConfigFactory
import pureconfig.{ConfigReader, ConfigSource, ConvertHelpers}
import pureconfig.generic.derivation.default._
// import pureconfig.module.http4s._
import cats.effect.{Resource, Sync}
import com.comcast.ip4s._
import pureconfig.module.catseffect.loadF

object Configuration {

    implicit val myPortReader: ConfigReader[Port] = ConfigReader.fromString[Port] {
        ConvertHelpers.optF(Port.fromString(_))
    }

    sealed trait Config derives ConfigReader
    case class ServiceConf(server: ServerConfig, database: DatabaseConfig) extends Config
    sealed trait SubConfig derives ConfigReader
    case class ServerConfig(port: Port) extends SubConfig
    case class DatabaseConfig(driver: String, url: String, user: String, password: String, threadPoolSize: Int) extends SubConfig


    def load[F[_]: Sync](configFile: String = "application.conf"): Resource[F, Config] =
        Resource.eval(loadF[F,Config](  ConfigSource.fromConfig(ConfigFactory.load(configFile))))



}

