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

import models.common.AddressHelper.{addressJsonReads, addressJsonWrites}
import models.common.NameHelper.{nameJsonReads, nameJsonWrites}
import models.common._
import models.common.reference.Reference
import org.maikalal.common.util.ArangodbDatabaseUtility
import org.maikalal.common.util.ArangodbDatabaseUtility.{DBDocuments, DBEdges}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json.{Writes, _}
import play.api.libs.ws.WSClient

import scala.concurrent.Future

/**
  * Created by pratimsc on 31/12/15.
  */
case class Guardian(guardian_id: String, guardian_name: Name, address: Address, gender: String, status: String, email: Option[String], national_insurance_number: Option[String])

case class GuardianStudentRelationShip(student: Student, relationship: String)

case class GuardianRegistrationData(guardian_name: Name, address: Address, gender: String, student_relationship: String, email: Option[String], national_insurance_number: Option[String])

case class GuardianStudentRelationShipRegistrationData(school_id: String, student_id: String, guardian_id: String, relationship: String)

object GuardianHelper {

  val guardianFormMapping = mapping(
    "name" -> NameHelper.nameMapping,
    "address" -> AddressHelper.addressFormMapping,
    "gender" -> nonEmptyText(minLength = 1, maxLength = 1),
    "student_relationship" -> text,
    "email" -> optional(email),
    "national_insurance" -> optional(text(minLength = 8, maxLength = 8))
  )(GuardianRegistrationData.apply)(GuardianRegistrationData.unapply)

  val registerGuardianForm = Form(guardianFormMapping)

  val guardianStudentRelationshipFormMapping = mapping(
    "school_id" -> text,
    "student_id" -> text,
    "guardian_id" -> text,
    "student_relationship" -> text
  )(GuardianStudentRelationShipRegistrationData.apply)(GuardianStudentRelationShipRegistrationData.unapply)

  implicit val guardianRegDataJsonWrites: Writes[GuardianRegistrationData] = (
    (__ \ "name").write[Name] and
      (__ \ "address").write[Address] and
      (__ \ "gender").write[String] and
      (__ \ "student_relationship").write[String] and
      (__ \ "email").writeNullable[String] and
      (__ \ "national_insurance_number").writeNullable[String]
    ) (unlift(GuardianRegistrationData.unapply))

  implicit val guardianJsonWrites: Writes[Guardian] = (
    (__ \ "_id").write[String] and
      (__ \ "name").write[Name] and
      (__ \ "address").write[Address] and
      (__ \ "gender").write[String] and
      (__ \ "student_relationship").write[String] and
      (__ \ "email").writeNullable[String] and
      (__ \ "national_insurance_number").writeNullable[String]
    ) (unlift(Guardian.unapply))
  implicit val guardianJsonReads: Reads[Guardian] = (
    (__ \ "_id").read[String] and
      (__ \ "name").read[Name] and
      (__ \ "address").read[Address] and
      (__ \ "gender").read[String] and
      (__ \ "student_relationship").read[String] and
      (__ \ "email").readNullable[String] and
      (__ \ "national_insurance_number").readNullable[String]
    ) (Guardian.apply _)


  /*
   * A non persistence storage for Schools
   */
  //val guardianList = scala.collection.mutable.MutableList[Guardian]()

  def findAllBySchool(school_id: String)(implicit ws: WSClient): Future[List[Guardian]] = {
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |filter sc._id == "${school_id}" && sc.status != "${Reference.STATUS.DELETE}"
         |FOR e in ${DBEdges.SCHOOL_DEALS_WITH_GUARDIAN}
         |filter e._from == sc._id
         |FOR gu in ${DBDocuments.GUARDIANS}
         |filter gu._id == e._to && gu.status != "${Reference.STATUS.DELETE}"
         |return gu
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the guardians based on the school id [${school_id}] is -> ${res.json}")
      val guardians: List[Guardian] = (res.json \ "result").as[List[Guardian]]
      guardians
    }
  }

  def findAllByStudent(student_id: String)(implicit ws: WSClient): Future[List[Guardian]] = {
    val aql =
      s"""
         |FOR st in ${DBDocuments.SCHOOLS}
         |filter st._id == "${student_id}" && st.status != "${Reference.STATUS.DELETE}"
         |FOR e in ${DBEdges.STUDENT_RELATED_TO_GUARDIAN}
         |filter e._from == st._id
         |FOR gu in ${DBDocuments.GUARDIANS}
         |filter gu._id == e._to && gu.status != "${Reference.STATUS.DELETE}"
         |return gu
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the guardians based on the student id [${student_id}] is -> ${res.json}")
      val guardians: List[Guardian] = (res.json \ "result").as[List[Guardian]]
      guardians
    }
  }

  def findById(guardian_id: String)(implicit ws: WSClient): Future[Option[Guardian]] = {
    val aql =
      s"""
         |FOR gu in ${DBDocuments.GUARDIANS}
         |filter gu._id == '${guardian_id}' && gu.status != "${Reference.STATUS.DELETE}"
         |return gu
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the guardian based on the id [${guardian_id}] is -> ${res.json}")
      val guardians: List[Guardian] = (res.json \ "result").as[List[Guardian]]
      guardians match {
        case h :: t => Some(h)
        case _ => None
      }
    }
  }

  def findByIdAndSchool(guardian_id: String, school_id: String)(implicit ws: WSClient): Future[Option[Guardian]] = {
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |filter sc._id == "${school_id}" && sc.status != "${Reference.STATUS.DELETE}"
         |FOR e in ${DBEdges.SCHOOL_DEALS_WITH_GUARDIAN}
         |filter e._from == sc._id
         |FOR gu in ${DBDocuments.GUARDIANS}
         |filter gu._id == e._to && gu._id == "${guardian_id}"  && gu.status != "${Reference.STATUS.DELETE}"
         |return gu
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the guardian based on the id [${guardian_id}] is -> ${res.json}")
      val guardians: List[Guardian] = (res.json \ "result").as[List[Guardian]]
      guardians match {
        case h :: t => Some(h)
        case _ => None
      }
    }
  }

  def addGuardian(g: GuardianRegistrationData, student_id: String, school_id: String)(implicit ws: WSClient): Future[Option[String]] = {
    val g_json: JsValue = Json.toJson(g).as[JsObject] +("status", JsString(Reference.STATUS.ACTIVE))
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |  FILTER sc._id == "${school_id}" && sc.status != "${Reference.STATUS.DELETE}"
         |  FOR st in ${DBDocuments.STUDENTS}
         |  FILTER st._id == "${student_id}" && st.status != "${Reference.STATUS.DELETE}"
         |  INSERT ${g_json} in ${DBDocuments.GUARDIANS}
         |  let gu = NEW
         |  INSERT {"_from":sc._id, "_to":gu._id}in ${DBEdges.SCHOOL_DEALS_WITH_GUARDIAN}
         |  let rel1 = NEW
         |  INSERT {"_from":st._id, "_to":gu._id} in ${DBEdges.STUDENT_RELATED_TO_GUARDIAN}
         |  let rel2 = NEW
         |return {"school":sc, "relation_1":rel1, "student":st, "relation_2":rel2, "guardian":gu}
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the guardian added ->${Json.prettyPrint(res.json)}")
      val result = (res.json \ "result") (0)
      val sc_json = result \ "school"
      val st_json = result \ "student"
      val gu_json = result \ "guardian"
      val guardian_id = (gu_json \ "_id").as[String]
      Some(guardian_id)
    }
  }

  def updateGuardianStudentRelationship(gsr: GuardianStudentRelationShipRegistrationData)(implicit ws: WSClient): Future[Some[Guardian]] = ???

}