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
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Hours, Period}
import play.api.Play
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by pratimsc on 31/12/15.
  */
object DateUtility {

  val key_date_format: String = Play.current.configuration.getString("application.date.display.format").getOrElse("YYYY-MM-dd")

  def fommattedDate(date: DateTime, format: String = key_date_format): String = DateTimeFormat.forPattern(format).print(date)

  val hourFormMapping: Mapping[Hours] = mapping(
    "hours" -> number
  )((h: Int) => Hours.hours(h))((h: Hours) => Some(h.getHours).asInstanceOf[Option[Int]])

  val periodFormMapping: Mapping[Period] = mapping(
    "period" -> text
  )((p) => Period.parse(p))((p) => Some(p.toString).asInstanceOf[Option[String]])

  implicit val hourJsonWrites: Writes[Hours] = (__ \ "hours").write[Int].contramap(_.getHours)
  implicit val hourJsonReads: Reads[Hours] = (__ \ "hours").read[Int].map(Hours.hours _)

  implicit val periodJsonWrites: Writes[Period] = (__ \ "period").write[String].contramap(_.toString)
  implicit val periodJsonReads: Reads[Period] = (__ \ "period").read[String].map(Period.parse _)

}

object MoneyUtility {
  val moneyFormMapping: Mapping[Money] = mapping(
    "currency" -> nonEmptyText(minLength = 3, maxLength = 3).transform((c: String) => CurrencyUnit.of(c), (c: CurrencyUnit) => c.toString),
    "amount" -> bigDecimal(10, 2)
  )((c, d) => Money.parse(c.toString + d.toString()))(m => Some((m.getCurrencyUnit, m.getAmount)).asInstanceOf[Option[(CurrencyUnit, BigDecimal)]])

  implicit val moneyJsonWrites = new Writes[Money] {
    override def writes(m: Money): JsValue = Json.obj(
      "currency" -> m.getCurrencyUnit.getCurrencyCode,
      "amount" -> m.getAmount.doubleValue())
  }

  implicit val moneyJsonReads: Reads[Money] = new Reads[Money] {
    override def reads(json: JsValue): JsResult[Money] = {
      try {
        JsSuccess(Money.of(CurrencyUnit.of((json \ "currency").as[String]), (json \ "amount").as[Double]))
      } catch {
        case e: Throwable => JsError(e.getMessage)
      }
    }
  }
}
