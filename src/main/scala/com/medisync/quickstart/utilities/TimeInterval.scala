package com.medisync.quickstart.utilities

import java.time.LocalTime
import spire.algebra.Order
import spire.math.Interval
import spire.math.interval._
import spire.math.extras.interval.IntervalSeq
import io.circe.{Encoder, Decoder, Json}
import io.circe.syntax._
import scala.util.Try
import java.time.LocalDate

object TimeIntervals:


  object LocalDateVar {
    def unapply(str: String): Option[LocalDate] = {
      if (!str.isEmpty)
        Try(LocalDate.parse(str)).toOption
      else
        None
    }
  }


  given Order[LocalTime] = Order.from[LocalTime](_ compareTo _)

  type LocalTimeInterval = Interval[LocalTime]

  object LocalTimeInterval:
    def create(start: LocalTime, end: LocalTime): Interval[LocalTime] =
      Interval.openLower(start, end)

  extension (in: LocalTimeInterval)
    def lower: LocalTime = in.lowerBound.asInstanceOf[ValueBound[LocalTime]].a
    def upper: LocalTime = in.upperBound.asInstanceOf[ValueBound[LocalTime]].a

  given Encoder[LocalTimeInterval] = new Encoder[LocalTimeInterval]:
    final def apply(a: LocalTimeInterval): Json =
      Json.obj(
        ("start", a.lower.asJson),
        ("end", a.upper.asJson)
      )

  given Decoder[LocalTimeInterval] =
    Decoder.forProduct2[LocalTimeInterval, LocalTime, LocalTime](
      "start",
      "end"
    ) { (a, b) => LocalTimeInterval.create(a, b) }


  type LocalTimeMultiInterval = IntervalSeq[LocalTime]

  object LocalTimeMultiInterval:
    def empty: LocalTimeMultiInterval = IntervalSeq.empty[LocalTime]

  given Encoder[LocalTimeMultiInterval] = new Encoder[LocalTimeMultiInterval]:
    final def apply(a: LocalTimeMultiInterval): Json = 
      a.intervals.toList.asJson

  given Decoder[LocalTimeMultiInterval] = 
    Decoder[List[LocalTimeInterval]].map(_.foldLeft(LocalTimeMultiInterval.empty)(_ | _))
    
