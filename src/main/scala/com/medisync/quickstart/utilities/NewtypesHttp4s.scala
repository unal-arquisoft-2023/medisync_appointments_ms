package com.medisync.quickstart.utilities

import cats.Show
import monix.newtypes._
import org.http4s._

object NewtypesHttp4s {
  trait DerivedHttp4ParamDecoder:
    given newtypesParamDecoder[T, S](using
        B: HasBuilder.Aux[T, S],
        E: HasExtractor.Aux[T, S],
        C: QueryParamDecoder[S],
        V: Show[S]
    ): QueryParamDecoder[T] =
      C.emap(s =>
        B.build(s)
          .left
          .map(ex =>
            val errMsg = s"Invalid ${ex.message.fold("")(m => s" — $m")}"
            ParseFailure(errMsg, errMsg)
          )
      )
  trait DerivedHttp4sParamEncoder:
    given newtypesParamEncoder[T, S](using
        B: HasBuilder.Aux[T, S],
        E: HasExtractor.Aux[T, S],
        C: QueryParamEncoder[S],
        V: Show[S]
    ): QueryParamEncoder[T] =
      C.contramap(t => E.extract(t))

  trait DerivedHttp4sParamCodec
      extends DerivedHttp4ParamDecoder
      with DerivedHttp4sParamEncoder

}
