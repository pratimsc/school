import org.joda.time.Period

val periods = List(Period.seconds(1), Period.minutes(1), Period.hours(1),
  Period.days(1), Period.weeks(1), Period.months(1), Period.years(1), Period.ZERO
)

val ptype = periods.map(_.toString)

val parse = ptype.map(Period.parse(_))