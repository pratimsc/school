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

import models.common.{Address, AddressHelper, AddressRegistrationData, Name}
import org.joda.time.DateTime


case class Student(student_id: Long, school: School, name: Name, status: String, gender: String, address: Address, dob: DateTime, email: Option[String], ethnicity: Option[String], sen_code: Option[String])

case class StudentRegistrationData(school_id: Long, name: Name, status: String, gender: String, address: AddressRegistrationData, dob: DateTime, email: Option[String], ethnicity: Option[String], sen_code: Option[String])

object StudentHelper {

  /*
   * A non persistence storage for Schools
   */
  val studentList = scala.collection.mutable.MutableList[Student](
    Student(1, SchoolHelper.findById(1).get, Name("George", Some("Washington"), "DC"), "A", "M", AddressHelper.findById(3).get, DateTime.parse("2012-1-1"), Some("george.dc.washington@blahblah.com"), Some("White American"), Some("Sen code 01")),
    Student(2, SchoolHelper.findById(1).get, Name("Neon", Some("Anderson"), "Matrix"), "A", "M", AddressHelper.findById(4).get, DateTime.parse("2012-1-2"), Some("leon.matrix.anderson@blahblah.com"), Some("White American"), Some("Sen code 01")),
    Student(3, SchoolHelper.findById(2).get, Name("Lisa", Some("Butcher"), "Gamer"), "A", "F", AddressHelper.findById(5).get, DateTime.parse("2012-1-3"), Some("lisa.gamer.butcher@blahblah.com"), Some("Black American"), Some("Sen code 01")),
    Student(4, SchoolHelper.findById(2).get, Name("Bhima", Some("Pandu"), "Mahabharata"), "A", "M", AddressHelper.findById(6).get, DateTime.parse("2012-1-4"), Some("bhima.mahabharata.pandu@blahblah.com"), Some("Ancient Indian"), Some("Sen code 01")),
    Student(5, SchoolHelper.findById(2).get, Name("Thor", Some("Hammer"), "Pagan God"), "A", "M", AddressHelper.findById(7).get, DateTime.parse("2012-1-5"), Some("thor.pagan.god.hammer@blahblah.com"), Some("Ancient God"), Some("Sen code 01"))
  )

  def findAll(school_id: Long): List[Student] = studentList.filter(_.school.school_id == school_id).toList

  def findById(student_id: Long, school_id: Long): Option[Student] = {
    SchoolHelper.findById(school_id) match {
      case None => None
      case Some(school) => studentList.find(s => s.student_id == student_id && s.school.school_id == school.school_id)
    }
  }

  def findAllStudentsByGuardianId(guardian_id: Long): List[Student] = GuardianHelper.findById(guardian_id) match {
    case Some(guardian) =>
      guardian.students.map(_.student)
    case None => Nil
  }

  def addStudent(s: StudentRegistrationData): Option[Student] = SchoolHelper.findById(s.school_id) match {
    case None => None
    case Some(school) =>
      val address = AddressHelper.addAddress(s.address)
      val student = Student(studentList.last.student_id + 1, school, s.name, s.status, s.gender, address, s.dob, s.email, s.ethnicity, s.sen_code)
      studentList += student
      Some(student)
  }

}