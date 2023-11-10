package com.medisync.quickstart.utilities

import cats.Show
import monix.newtypes.*
import doobie.{Get, Put}

object NewtypesDoobie:
  trait DerivedDoobieCodec:
    given newtypesGet[T, S](using
        B: HasBuilder.Aux[T, S],
        G: Get[S],
        V: Show[S]
    ): Get[T] =
      G.temap(s =>
        B.build(s)
          .left
          .map(ex => s"Invalid ${ex.message.fold("")(m => s" — $m")}")
      )

    given newtypesPut[T, S](using
        E: HasExtractor.Aux[T, S],
        P: Put[S]
    ): Put[T] =
      P.contramap(t => E.extract(t))
