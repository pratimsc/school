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

package org.maikalal.common.util

import org.joda.money.{CurrencyUnit, Money}
import org.joda.time._
import org.joda.time.format.DateTimeFormat
import play.api.Play
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by pratimsc on 31/12/15.
  */
object DateUtility {

  lazy val key_date_format: String = Play.current.configuration.getString("application.date.display.format").getOrElse("YYYY-MM-dd")

  lazy val hourFormMapping: Mapping[Hours] = mapping(
    "hours" -> number
  )((h: Int) => Hours.hours(h))((h: Hours) => Some(h.getHours).asInstanceOf[Option[Int]])

  lazy val periodFormMapping: Mapping[Period] = mapping(
    "period" -> text
  )((p) => Period.parse(p))((p) => Some(p.toString).asInstanceOf[Option[String]])

  lazy implicit val hourJsonWrites: Writes[Hours] = (__ \ "hours").write[Int].contramap(_.getHours)
  lazy implicit val hourJsonReads: Reads[Hours] = (__ \ "hours").read[Int].map(Hours.hours _)

  lazy implicit val periodJsonWrites: Writes[Period] = (__ \ "period").write[String].contramap(_.toString)
  lazy implicit val periodJsonReads: Reads[Period] = (__ \ "period").read[String].map(Period.parse _)

  def fommattedDate(date: DateTime, format: String = key_date_format): String = DateTimeFormat.forPattern(format).print(date)

  def datesBetween(first: DateTime, second: DateTime): List[DateTime] = {
    def dates(f: DateTime, l: DateTime, dl: List[DateTime]): List[DateTime] = {
      if (f.isBefore(l))
        dates(f.plusDays(1), l, f :: dl)
      else
        l :: dl
    }
    if (first.isBefore(second))
      dates(first, second, List())
    else
      dates(second, first, List())
  }

  def dateIsWeekend(date: DateTime): Boolean = dateIsSaturday(date) || dateIsSunday(date)

  def dateIsSaturday(date: DateTime): Boolean = (date.getDayOfWeek == 6)

  def dateIsSunday(date: DateTime): Boolean = (date.getDayOfWeek == 7)

  def datesAreSame(first: DateTime, second: DateTime, zone: DateTimeZone = DateTimeZone.UTC): Boolean = {
    val ld1 = new LocalDate(first.withZone(zone))
    val ld2 = new LocalDate(second.withZone(zone))
    ld1.equals(ld2)
  }

  def dateIsHoliday(date: DateTime, holidays: List[DateTime]) = holidays.filter(datesAreSame(_, date)).size != 0
}


object MoneyUtility {
  lazy val moneyFormMapping: Mapping[Money] = mapping(
    "currency" -> nonEmptyText(minLength = 3, maxLength = 3).transform((c: String) => CurrencyUnit.of(c), (c: CurrencyUnit) => c.toString),
    "amount" -> bigDecimal(10, 2)
  )((c, d) => Money.parse(c.toString + d.toString()))(m => Some((m.getCurrencyUnit, m.getAmount)).asInstanceOf[Option[(CurrencyUnit, BigDecimal)]])

  lazy implicit val moneyJsonWrites = new Writes[Money] {
    override def writes(m: Money): JsValue = Json.obj(
      "currency" -> m.getCurrencyUnit.getCurrencyCode,
      "amount" -> m.getAmount.doubleValue())
  }

  lazy implicit val moneyJsonReads: Reads[Money] = new Reads[Money] {
    override def reads(json: JsValue): JsResult[Money] = {
      try {
        JsSuccess(Money.of(CurrencyUnit.of((json \ "currency").as[String]), (json \ "amount").as[Double]))
      } catch {
        case e: Throwable => JsError(e.getMessage)
      }
    }
  }
}
