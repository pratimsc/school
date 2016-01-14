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

import org.maikalal.common.util.DatabaseUtility
import play.api.data.Forms._

/**
  * Created by pratimsc on 29/12/15.
  */
case class Address(address_id: Long, first_line: Option[String], second_line: Option[String], city: Option[String], county: Option[String], country: String, zip_code: String)

case class AddressRegistrationData(first_line: Option[String], second_line: Option[String], city: Option[String], county: Option[String], country: String, zip_code: String)


object AddressHelper {
  /*
   * A non persistence storage for Address
   */
  var addressList = scala.collection.mutable.MutableList[Address](
    Address(1, Some("1st Avenue"), Some("Second Avenue"), Some("Maya City"), Some("Gondura County"), "United Kindom", "UK11 99MB"),
    Address(2, Some("1st Road"), Some("Second Road"), Some("Boga City"), Some("Mejo County"), "United Kindom", "UK11 99KK"),
    Address(3, Some("Add 3 first road"), Some("Add 3 Second Road"), Some("Boga City"), Some("Mejo County"), "United Kindom", "UK11 99KK"),
    Address(4, Some("Add 4 first Road"), Some("Second Road"), Some("Boga City"), Some("Student County"), "United Kindom", "UK11 99KK"),
    Address(5, Some("Add 5 1st Road"), Some("Second Road"), Some("Boga City"), Some("Student County"), "United Kindom", "UK11 99KK"),
    Address(6, Some("Add 6 1st Road"), Some("Second Road"), Some("Boga City"), Some("Student County"), "United Kindom", "UK11 99KK"),
    Address(7, Some("Add 7 1st Road"), Some("Second Road"), Some("Boga City"), Some("Student County"), "United Kindom", "UK11 99KK")
  )

  def findAll: List[Address] = addressList.toList

  def findById(address_id: Long): Option[Address] = addressList.find(_.address_id == address_id)

  def manufactureAddress(a: AddressRegistrationData): Option[Address] = DatabaseUtility.getUniqueAddressId match {
    case Some(address_id) =>
      val address = Address(address_id, a.first_line, a.second_line, a.city, a.county, a.country, a.zip_code)
      addressList += address
      Some(address)
    case None => None
  }

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
  )(AddressRegistrationData.apply)(AddressRegistrationData.unapply)
}