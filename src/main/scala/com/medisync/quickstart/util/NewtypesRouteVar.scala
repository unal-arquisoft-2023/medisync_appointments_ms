package com.medisync.quickstart
import monix.newtypes._
import scala.util.Try

object NewtypesRouteVar: 
    trait RouteUnapplicable[T]:
        def unapply(value: String): Option[T]
    
    def Var[T](using v: RouteUnapplicable[T]) = v

    def VarHelper[T,S](f: String => Option[S])(using B: HasBuilder.Aux[T,S]) = 
        val g: String => Option[T] = v => f(v).map(B.build(_).toOption).flatten
        new RouteUnapplicable[T]:
            def unapply(value: String) = g(value)

    trait DeriveRouteUnnaplicable[S]:
        def fromString(value: String): Option[S]
        given ru[T](using B: HasBuilder.Aux[T,S]): RouteUnapplicable[T] = 
            VarHelper[T,S](fromString)
    
    given [T](using B: HasBuilder.Aux[T,Int]): RouteUnapplicable[T] = 
        VarHelper[T,Int](v => Try(v.toInt).toOption)

