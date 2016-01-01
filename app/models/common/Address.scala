package models.common

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

  def addAddress(a: AddressRegistrationData): Address = {
    val address = addressList.toList match {
      case Nil => Address(1, a.first_line, a.second_line, a.city, a.county, a.country, a.zip_code)
      case _ => Address(addressList.last.address_id + 1, a.first_line, a.second_line, a.city, a.county, a.country, a.zip_code)
    }
    addressList += address
    address
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