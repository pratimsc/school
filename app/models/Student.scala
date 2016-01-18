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

import models.SchoolHelper.schoolJsonReads
import models.common.AddressHelper.{addressJsonReads, addressJsonWrites}
import models.common.NameHelper.{nameJsonReads, nameJsonWrites}
import models.common._
import models.common.reference.Reference
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

import scala.concurrent.Future


case class Student(student_id: String, student_name: Name, status: String, gender: String, address: Address, dob: DateTime, email: Option[String], ethnicity: Option[String], sen_code: Option[String])

case class StudentRegistrationData(student_name: Name, gender: String, address: Address, dob: DateTime, email: Option[String], ethnicity: Option[String], sen_code: Option[String])

object StudentHelper {

  val studentFormMapping = mapping(
    "name" -> NameHelper.nameMapping,
    "gender" -> text(minLength = 1, maxLength = 1),
    "address" -> AddressHelper.addressFormMapping,
    "date_of_birth" -> jodaDate("yyyy-MM-dd"),
    "email" -> optional(email),
    "ethnicity" -> optional(text),
    "sen_code" -> optional(text)
  )(StudentRegistrationData.apply)(StudentRegistrationData.unapply)

  val registerStudentForm = Form(studentFormMapping)

  implicit val studentRegDataJsonWrites: Writes[StudentRegistrationData] = (
    (__ \ "name").write[Name] and
      (__ \ "gender").write[String] and
      (__ \ "address").write[Address] and
      (__ \ "dob").write[DateTime] and
      (__ \ "email").writeNullable[String] and
      (__ \ "ethnicity").writeNullable[String] and
      (__ \ "sen_code").writeNullable[String]
    ) (unlift(StudentRegistrationData.unapply))


  implicit val studentJsonWrites: Writes[Student] = (
    (__ \ "_id").write[String] and
      (__ \ "name").write[Name] and
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
      (__ \ "name").read[Name] and
      (__ \ "status").read[String] and
      (__ \ "gender").read[String] and
      (__ \ "address").read[Address] and
      (__ \ "dob").read[DateTime] and
      (__ \ "email").readNullable[String] and
      (__ \ "ethnicity").readNullable[String] and
      (__ \ "sen_code").readNullable[String]
    ) (Student.apply _)

  implicit val studentSchoolComboJsonReads: Reads[Tuple2[Student, School]] = (
    (__ \ "student").read[Student] and
      (__ \ "school").read[School]
    ) tupled

  def findAllBySchool(school_id: String)(implicit ws: WSClient): Future[List[Student]] = {
    val aql =
      s"""
         |FOR sc in schools
         |filter sc._id == "${school_id}" && sc.status != "${Reference.STATUS.DELETE}"
         |FOR e in enrolled
         |filter e._from == sc._id
         |FOR st in students
         |filter st._id == e._to && st.status != "${Reference.STATUS.DELETE}"
         |return st
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the students based on the school [${school_id}] is -> ${res.json}")
      val students: List[Student] = (res.json \ "result").as[List[Student]]
      students
    }
  }

  def findByIdAndSchool(student_id: String, school_id: String)(implicit ws: WSClient): Future[Option[Student]] = {
    val aql =
      s"""
         |FOR sc in schools
         |filter sc._id == "${school_id}" && sc.status != "${Reference.STATUS.DELETE}"
         |FOR e in enrolled
         |filter e._from == sc._id
         |FOR st in students
         |filter st._id == e._to && st._id == "${student_id}"  && st.status != "${Reference.STATUS.DELETE}"
         |return st
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the student based on the id [${student_id}] is -> ${res.json}")
      val students: List[Student] = (res.json \ "result").as[List[Student]]
      students match {
        case h :: t =>
          Logger.debug(s"Details of the student extracted ${h}")
          Some(h)
        case _ => None
      }
    }
  }


  def findAllStudentsByGuardianId(guardian_id: String)(implicit ws: WSClient): Future[List[(Student, School)]] = {
    val aql =
      s"""
         |FOR gu in guardians
         |filter gu._id == "${guardian_id}"  && gu.status != "${Reference.STATUS.DELETE}"
         |FOR e1 in related_to
         |filter e1._to == gu._id
         |FOR st in students
         |filter st._id == e1._from && st.status != "${Reference.STATUS.DELETE}"
         |FOR e2 in enrolled
         |filter e2._to == st._id
         |FOR sc in schools
         |filter sc._id == e2._from && sc.status != "${Reference.STATUS.DELETE}"
         |return {"student":st,"school":sc}
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the students based on the id [${guardian_id}] is -> ${res.json}")
      val students: List[(Student, School)] = (res.json \ "result").as[List[(Student, School)]]
      students
    }
  }

  def addStudent(s: StudentRegistrationData, school_id: String)(implicit ws: WSClient): Future[Some[String]] = {
    val st_json: JsValue = Json.toJson(s).as[JsObject] +("status", JsString(Reference.STATUS.ACTIVE))
    val aql =
      s"""
         |FOR sc in schools
         |  FILTER sc._id == "${school_id}" && sc.status != "${Reference.STATUS.DELETE}"
         |  INSERT ${st_json} in students
         |  let st = NEW
         |  INSERT {
         |  "_from":sc._id,
         |  "_to":st._id
         |  }in enrolled
         |  let rel = NEW
         |return {"student":st, "school":sc, "relation":rel}
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the student added ->${Json.prettyPrint(res.json)}")
      val result = (res.json \ "result") (0)
      val st_json = result \ "student"
      val sc_json = result \ "school"
      val student_id = (st_json \ "_id").as[String]
      Some(student_id)
    }
  }
}