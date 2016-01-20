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

package models.common

import play.api.data.Forms._
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by pratimsc on 29/12/15.
  */
case class Address(first_line: Option[String], second_line: Option[String], city: Option[String], county: Option[String], country: String, zip_code: String)

object AddressHelper {
  /*
  * Some helper functions for operations on address
   */

  val addressFormMapping = mapping(
    "first_line" -> optional(text(maxLength = Char.MaxValue)),
    "second_line" -> optional(text(maxLength = Char.MaxValue)),
    "city" -> optional(text(maxLength = Char.MaxValue)),
    "county" -> optional(text(maxLength = Char.MaxValue)),
    "country" -> nonEmptyText(maxLength = Char.MaxValue),
    "zip_code" -> nonEmptyText(minLength = 6, maxLength = Char.MaxValue)
  )(Address.apply)(Address.unapply)

  implicit val addressJsonWrites: Writes[Address] = (
    (JsPath \ "first_line").writeNullable[String] and
      (JsPath \ "second_line").writeNullable[String] and
      (JsPath \ "city").writeNullable[String] and
      (JsPath \ "county").writeNullable[String] and
      (JsPath \ "country").write[String] and
      (JsPath \ "zip_code").write[String]
    ) (unlift(Address.unapply))

  implicit val addressJsonReads: Reads[Address] = (
    (JsPath \ "first_line").readNullable[String] and
      (JsPath \ "second_line").readNullable[String] and
      (JsPath \ "city").readNullable[String] and
      (JsPath \ "county").readNullable[String] and
      (JsPath \ "country").read[String] and
      (JsPath \ "zip_code").read[String]
    ) (Address.apply _)
}