package com.medisync.quickstart

import com.medisync.quickstart.Configuration.DatabaseConfig
import cats.effect.kernel.Async
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import cats.effect.Resource
import cats.syntax._
import org.flywaydb.core.Flyway


object Database {
    def transactor[F[_]: Async](config: DatabaseConfig): Resource[F,HikariTransactor[F]] =
        for {
            ec <- ExecutionContexts.fixedThreadPool(config.threadPoolSize)
            xa <- HikariTransactor.newHikariTransactor[F](
                driverClassName = config.driver,
                url = config.url,
                user = config.user,
                pass = config.password,
                connectEC = ec
            )
        } yield xa

    def initialize[F[_]: Async](transactor: HikariTransactor[F]): F[Unit] = 
        transactor.configure{ dataSource => 
            Async[F].blocking {
                val flyway = Flyway.configure().dataSource(dataSource).load()
                flyway.migrate()
                ()
            } 
        }
}