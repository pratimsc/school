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

import models.common._
import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by pratimsc on 31/12/15.
  */
case class Guardian(guardian_id: Long, name: Name, address: Address, gender: String, status: String, email: Option[String], national_insurance_number: Option[String], students: List[GuardianStudentRelationShip])

case class GuardianStudentRelationShip(student: Student, relationship: String)

case class GuardianRegistrationData(name: Name, address: AddressRegistrationData, gender: String, student_relationship: String, email: Option[String], national_insurance_number: Option[String])

case class GuardianStudentRelationShipRegistrationData(school_id: Long, student_id: Long, guardian_id: Long, relationship: String)

object GuardianHelper {

  val guardianFormMapping = mapping(
    "guardian_name" -> NameHelper.nameMapping,
    "guardian_address" -> AddressHelper.addressFormMapping,
    "gender" -> nonEmptyText(minLength = 1, maxLength = 1),
    "student_relationship" -> text,
    "email" -> optional(email),
    "national_insurance" -> optional(text(minLength = 8, maxLength = 8))
  )(GuardianRegistrationData.apply)(GuardianRegistrationData.unapply)

  val guardianStudentRelationshipFormMapping = mapping(
    "school_id" -> longNumber,
    "student_id" -> longNumber,
    "guardian_id" -> longNumber,
    "student_relationship" -> text
  )(GuardianStudentRelationShipRegistrationData.apply)(GuardianStudentRelationShipRegistrationData.unapply)

  val registerGuardianForm = Form(guardianFormMapping)

  /*
   * A non persistence storage for Schools
   */
  val guardianList = scala.collection.mutable.MutableList[Guardian](
    Guardian(1, Name("Chocko", Some("Slaughter Man"), "Funny"), AddressHelper.findById(3).get, "M", "A", Some("chocko.funny@blahblah.com"), Some("NI12345781"), List(GuardianStudentRelationShip(StudentHelper.findById(1, 1).get, "Father"), GuardianStudentRelationShip(StudentHelper.findById(3, 2).get, "Father"))),
    Guardian(2, Name("Lisa", Some("Pussy Cat"), "Angry"), AddressHelper.findById(3).get, "F", "A", Some("lisa.angry@blahblah.com"), Some("NI12345782"), List(GuardianStudentRelationShip(StudentHelper.findById(3, 2).get, "Mother"))),
    Guardian(3, Name("Mocho", None, "Cobbler"), AddressHelper.findById(3).get, "M", "A", Some("mocho.cobbler@blahblah.com"), Some("NI1234578"), List(GuardianStudentRelationShip(StudentHelper.findById(2, 1).get, "Father"))),
    Guardian(4, Name("Zhinga", Some("Mohanlal"), "Baghmare"), AddressHelper.findById(3).get, "M", "A", Some("zhinga.bhagmare@blahblah.com"), Some("NI1234578"), List(GuardianStudentRelationShip(StudentHelper.findById(4, 2).get, "Father"), GuardianStudentRelationShip(StudentHelper.findById(5, 2).get, "Grand Father")))
  )

  /*
    Guardian 1 = Student(1, SchoolHelper.findById(1).get, Name("George", Some("Washington"), "DC"), "A", "M", AddressHelper.findById(3).get, DateTime.parse("2012-1-1"), Some("george.dc.washington@blahblah.com"), Some("White American"), Some("Sen code 01")),
    Guardian 3 = Student(2, SchoolHelper.findById(1).get, Name("Neon", Some("Anderson"), "Matrix"), "A", "M", AddressHelper.findById(4).get, DateTime.parse("2012-1-2"), Some("leon.matrix.anderson@blahblah.com"), Some("White American"), Some("Sen code 01")),
    Guardian 1,Guardian 2 = Student(3, SchoolHelper.findById(2).get, Name("Lisa", Some("Butcher"), "Gamer"), "A", "F", AddressHelper.findById(5).get, DateTime.parse("2012-1-3"), Some("lisa.gamer.butcher@blahblah.com"), Some("Black American"), Some("Sen code 01")),
    Guardian 4 = Student(4, SchoolHelper.findById(2).get, Name("Bhima", Some("Pandu"), "Mahabharata"), "A", "M", AddressHelper.findById(6).get, DateTime.parse("2012-1-4"), Some("bhima.mahabharata.pandu@blahblah.com"), Some("Ancient Indian"), Some("Sen code 01")),
    Guardian 4 = Student(5, SchoolHelper.findById(2).get, Name("Thor", Some("Hammer"), "Pagan God"), "A", "M", AddressHelper.findById(7).get, DateTime.parse("2012-1-5"), Some("thor.pagan.god.hammer@blahblah.com"), Some("Ancient God"), Some("Sen code 01"))
  )*/

  def findAllBySchool(school_id: Long): List[Guardian] = guardianList.filter(_.students.filter(_.student.school.school_id == school_id).isEmpty == false).toList

  def findAllByStudent(student_id: Long): List[Guardian] = guardianList.filter(_.students.filter(_.student.student_id == student_id).isEmpty == false).toList

  def findById(guardian_id: Long): Option[Guardian] = guardianList.find(_.guardian_id == guardian_id)

  def findById(guardian_id: Long, school_id: Long): Option[Guardian] = findById(guardian_id) match {
    case Some(guardian) =>
      if (guardian.students.find(_.student.school.school_id == school_id).isEmpty == false)
        Some(guardian)
      else
        None
    case None => None
  }

  //guardian_id: Long, name: Name, address: Address, gender: String, email: Option[String], national_insurance_number: Option[String], students: List[GuardianStudentRelationShip]

  def addGuardian(g: GuardianRegistrationData, student_id: Long, school_id: Long): Guardian = {
    val address = AddressHelper.addAddress(g.address)
    val student = StudentHelper.findById(student_id, school_id).get
    val guardian = Guardian(guardianList.last.guardian_id + 1, g.name, address, g.gender, "A", g.email, g.national_insurance_number, List(GuardianStudentRelationShip(student, g.student_relationship)))
    guardianList += guardian
    guardian
  }

  def updateGuardianStudentRelationship(gsr: GuardianStudentRelationShipRegistrationData): Guardian = {
    val student = StudentHelper.findById(gsr.student_id, gsr.school_id).get
    val guardian = findById(gsr.guardian_id).get
    val students = GuardianStudentRelationShip(student, gsr.relationship) :: guardian.students
    val new_guardian = guardian.copy(students = students)
    guardianList.dropWhile(_.guardian_id == new_guardian.guardian_id) += new_guardian
    new_guardian
  }

}