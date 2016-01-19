/*
 * Copyright [2016] Maikalal Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.common.time

/**
  * Created by pratimsc on 02/01/16.
  */

import org.joda.time.Period
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.json.{Reads, _}

/**
  * Enumeration of frequency base for utilizing with an RRule
  * Copied initially from - https://raw.githubusercontent.com/saddle/saddle/master/saddle-core/src/main/scala/org/saddle/time/Frequency.scala
  * Later on changed to a different implementation
  */


case class Frequency(frequency: Period)

object Frequency {

  implicit val frequencyJsonReads: Reads[Frequency] = (__).read[String].map(Frequency.parse(_))
  implicit val frequencyJsonWrites: Writes[Frequency] = (__).write[String].contramap(Frequency.printString(_))

  val SECONDLY = Frequency(Period.seconds(1))
  val MINUTELY = Frequency(Period.minutes(1))
  val HOURLY = Frequency(Period.hours(1))
  val DAILY = Frequency(Period.days(1))
  val WEEKLY = Frequency(Period.weeks(1))
  val MONTHLY = Frequency(Period.months(1))
  val YEARLY = Frequency(Period.years(1))
  val ONCE = Frequency(Period.ZERO)

  val frequencies = List(ONCE, SECONDLY, MINUTELY, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY)

  def parse(s: String): Frequency = s match {
    case "PT1S" => SECONDLY
    case "PT1M" => MINUTELY
    case "PT1H" => HOURLY
    case "P1D" => DAILY
    case "P1W" => WEEKLY
    case "P1M" => MONTHLY
    case "P1Y" => YEARLY
    case "PT0S" => ONCE
    case _ => ONCE
  }

  def printString(f: Frequency) = f.frequency.toString

}

