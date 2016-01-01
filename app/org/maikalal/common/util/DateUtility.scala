package org.maikalal.common.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.Play

/**
  * Created by pratimsc on 31/12/15.
  */
object DateUtility {

  val key_date_format = "application.date.display.format"

  def fommattedDate(date: DateTime, format: String = Play.current.configuration.getString(key_date_format)) = DateTimeFormat.forPattern(format).print(date)
}
