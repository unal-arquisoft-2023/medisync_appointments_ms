package com.medisync.quickstart

import com.medisync.quickstart.Configuration.DatabaseConfig
import cats.effect.kernel.Async
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import cats.effect.Resource
import org.flywaydb.core.Flyway
import cats.effect.kernel.Sync
import doobie.util.log._

object Database {
  def logHandler[F[_]: Async] = 
          new LogHandler[F]:
            def run(logEvent: LogEvent) =
              Async[F].blocking {
                println(logEvent.sql)
                println(logEvent.args)
              }
  def transactor[F[_]: Async](
      config: DatabaseConfig
  ): Resource[F, HikariTransactor[F]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool(config.threadPoolSize)
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = config.driver,
        url = config.url,
        user = config.user,
        pass = config.password,
        connectEC = ec,
        logHandler = Some(logHandler[F])
      )
    } yield xa

  def initialize[F[_]: Sync](transactor: HikariTransactor[F]): F[Unit] =
    transactor.configure { dataSource =>
      println("configggg")
      Sync[F].blocking {
        println("migrandoooo")
        val flyway = Flyway
          .configure()
          .validateMigrationNaming(true)
          .locations("classpath:db_migrations")
          .dataSource(dataSource)
          .load()
        flyway.migrate()
        ()
      }
    }
}
