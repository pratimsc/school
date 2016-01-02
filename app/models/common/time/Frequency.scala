package models.common.time

/**
  * Created by pratimsc on 02/01/16.
  */

import org.joda.time.Period

/**
  * Enumeration of frequency base for utilizing with an RRule
  */
sealed trait Frequency {
  this: Frequency =>
  protected[time] def toDur: Period = this match {
    case SECONDLY => Period.seconds(1)
    case MINUTELY => Period.minutes(1)
    case HOURLY => Period.hours(1)
    case DAILY => Period.days(1)
    case WEEKLY => Period.weeks(1)
    case MONTHLY => Period.months(1)
    case YEARLY => Period.years(1)
  }
}

case object SECONDLY extends Frequency

case object MINUTELY extends Frequency

case object HOURLY extends Frequency

case object DAILY extends Frequency

case object WEEKLY extends Frequency

case object MONTHLY extends Frequency

case object YEARLY extends Frequency
