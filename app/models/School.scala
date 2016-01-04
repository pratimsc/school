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

package models

import models.common.{Address, AddressHelper, AddressRegistrationData}
import play.api.data.Forms._
import play.api.data._

/**
  * Created by pratimsc on 28/12/15.
  */

case class School(school_id: Long, school_name: String, school_address: Address, status: String)

case class SchoolRegistration(school_name: String, school_address: AddressRegistrationData)

object SchoolHelper {

  val schoolFormMapping = mapping(
    "school_name" -> nonEmptyText(minLength = 10, maxLength = Char.MaxValue),
    "school_address" -> AddressHelper.addressFormMapping
  )(SchoolRegistration.apply)(SchoolRegistration.unapply)

  val registerSchoolForm = Form(schoolFormMapping)

  /*
   * A non persistence storage for Schools
   */
  val schoolList = scala.collection.mutable.MutableList[School](
    School(1, "Mongo Bongo School", AddressHelper.findById(1).get, "A"),
    School(2, "Tooko Takka School", AddressHelper.findById(2).get, "A")
  )

  def findAll: List[School] = schoolList.toList

  def findById(school: Long): Option[School] = schoolList.find(_.school_id == school)

  def findAllSchoolsByGuardianId(guardian_id: Long) = GuardianHelper.findById(guardian_id) match {
    case Some(guardian) =>
      guardian.students.groupBy(_.student.school).map(_._1).toList
    case None => Nil
  }

  def addSchool(s: SchoolRegistration): School = {
    val address = AddressHelper.addAddress(s.school_address)
    val school = schoolList.toList match {
      case Nil => School(1, s.school_name, address, "A")
      case _ => School(schoolList.last.school_id + 1, s.school_name, address, "A")
    }
    schoolList += school
    school
  }
}

