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

import models.SchoolHelper.{schoolJsonReads, schoolJsonWrites}
import models.common.AddressHelper.{addressJsonReads, addressJsonWrites}
import models.common.NameHelper.{nameJsonReads, nameJsonWrites}
import models.common._
import models.common.reference.Reference
import models.common.reference.Reference.DatabaseEdges
import org.joda.time.DateTime
import org.maikalal.common.util.ArangodbDatabaseUtility
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


case class Student(student_id: String, school: School, student_name: Name, status: String, gender: String, address: Address, dob: DateTime, email: Option[String], ethnicity: Option[String], sen_code: Option[String])

case class StudentRegistrationData(student_name: Name, gender: String, address: Address, dob: DateTime, email: Option[String], ethnicity: Option[String], sen_code: Option[String])

object StudentHelper {

  implicit val ws: WSClient = NingWSClient()

  val studentFormMapping = mapping(
    "student_name" -> NameHelper.nameMapping,
    "gender" -> text(minLength = 1, maxLength = 1),
    "student_address" -> AddressHelper.addressFormMapping,
    "date_of_birth" -> jodaDate("yyyy-MM-dd"),
    "email" -> optional(email),
    "ethnicity" -> optional(text),
    "sen_code" -> optional(text)
  )(StudentRegistrationData.apply)(StudentRegistrationData.unapply)

  val registerStudentForm = Form(studentFormMapping)

  implicit val studentRegDataJsonWrites: Writes[StudentRegistrationData] = (
    (__ \ "student_name").write[Name] and
      (__ \ "gender").write[String] and
      (__ \ "address").write[Address] and
      (__ \ "dob").write[DateTime] and
      (__ \ "email").writeNullable[String] and
      (__ \ "ethnicity").writeNullable[String] and
      (__ \ "sen_code").writeNullable[String]
    ) (unlift(StudentRegistrationData.unapply))


  implicit val studentJsonWrites: Writes[Student] = (
    (__ \ "student_id").write[String] and
      (__ \ "school").write[School] and
      (__ \ "student_name").write[Name] and
      (__ \ "status").write[String] and
      (__ \ "gender").write[String] and
      (__ \ "address").write[Address] and
      (__ \ "dob").write[DateTime] and
      (__ \ "email").writeNullable[String] and
      (__ \ "ethnicity").writeNullable[String] and
      (__ \ "sen_code").writeNullable[String]
    ) (unlift(Student.unapply))

  implicit val studentJsonReads: Reads[Student] = (
    (__ \ "_id").read[String] and
      (__ \ "school").read[School] and
      (__ \ "student_name").read[Name] and
      (__ \ "status").read[String] and
      (__ \ "gender").read[String] and
      (__ \ "address").read[Address] and
      (__ \ "dob").read[DateTime] and
      (__ \ "email").readNullable[String] and
      (__ \ "ethnicity").readNullable[String] and
      (__ \ "sen_code").readNullable[String]
    ) (Student.apply _)

  /*
   * A non persistence storage for Schools
   */
  val studentList = scala.collection.mutable.MutableList[Student](
    Student("1", Await.result(SchoolHelper.findById("schools/9677062879"), Duration.Inf).get, Name("George", Some("Washington"), "DC"), "A", "M", AddressHelper.addressList.get(3).get, DateTime.parse("2012-1-1"), Some("george.dc.washington@blahblah.com"), Some("White American"), Some("Sen code 01")),
    Student("2", Await.result(SchoolHelper.findById("schools/9677062879"), Duration.Inf).get, Name("Neon", Some("Anderson"), "Matrix"), "A", "M", AddressHelper.addressList.get(4).get, DateTime.parse("2012-1-2"), Some("leon.matrix.anderson@blahblah.com"), Some("White American"), Some("Sen code 01")),
    Student("3", Await.result(SchoolHelper.findById("schools/9677062879"), Duration.Inf).get, Name("Lisa", Some("Butcher"), "Gamer"), "A", "F", AddressHelper.addressList.get(5).get, DateTime.parse("2012-1-3"), Some("lisa.gamer.butcher@blahblah.com"), Some("Black American"), Some("Sen code 01")),
    Student("4", Await.result(SchoolHelper.findById("schools/9677062879"), Duration.Inf).get, Name("Bhima", Some("Pandu"), "Mahabharata"), "A", "M", AddressHelper.addressList.get(6).get, DateTime.parse("2012-1-4"), Some("bhima.mahabharata.pandu@blahblah.com"), Some("Ancient Indian"), Some("Sen code 01")),
    Student("5", Await.result(SchoolHelper.findById("schools/9677062879"), Duration.Inf).get, Name("Thor", Some("Hammer"), "Pagan God"), "A", "M", AddressHelper.addressList.get(7).get, DateTime.parse("2012-1-5"), Some("thor.pagan.god.hammer@blahblah.com"), Some("Ancient God"), Some("Sen code 01"))
  )

  def findAll(school_id: String): List[Student] = studentList.filter(_.school.school_id == school_id).toList

  def findById(student_id: String, school_id: String): Future[Nothing] = SchoolHelper.findById(school_id).flatMap { sc =>
    sc match {
      case Some(school) => None[Student]
      case None => None[Student]
    }
  }


  def findAllStudentsByGuardianId(guardian_id: Long): List[Student] = GuardianHelper.findById(guardian_id) match {
    case Some(guardian) =>
      guardian.students.map(_.student)
    case None => Nil
  }

  def addStudent(s: StudentRegistrationData, school_id: String)(implicit ws: WSClient): Future[Option[String]] = SchoolHelper.findById(school_id).flatMap { sc =>
    sc match {
      case Some(school) =>
        val st_json: JsValue = Json.toJson(s).as[JsObject] +("status", JsString(Reference.STATUS.ACTIVE))
        Logger.debug(s"Adding student ->[${st_json}]")
        ArangodbDatabaseUtility.databaseGraphApiVertexRequest(Reference.DatabaseVertex.STUDENT).post(st_json).map { res =>
          val student_id = (res.json \ "vertex" \ "_id").as[String]
          Logger.debug(s"Id of the student added -> ${student_id}")
          Logger.debug(s"Creating an Edge between School and Student")
          val sc_st_edge = Json.obj(
            "_from" -> JsString(school_id),
            "_to" -> JsString(student_id)
          )
          ArangodbDatabaseUtility.databaseGraphApiEdgeRequest(DatabaseEdges.SCHOOL_ENROLLED_STUDENT).post(sc_st_edge)
          Some(student_id)
        }
      case None =>
        Future.successful(None)
    }
  }
}