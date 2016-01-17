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
import models.common.reference.Reference
import models.common.{Address, AddressHelper}
import org.maikalal.common.util.GenericHelper
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.Future

/**
  * Created by pratimsc on 28/12/15.
  */

case class School(school_id: String, school_name: String, school_address: Address, status: String)

case class SchoolRegistration(school_name: String, school_address: Address)

object SchoolHelper {
  val schoolFormMapping = mapping(
    "school_name" -> nonEmptyText(minLength = 10, maxLength = Char.MaxValue),
    "school_address" -> AddressHelper.addressFormMapping
  )(SchoolRegistration.apply)(SchoolRegistration.unapply)

  implicit val schoolRegDataJsonWrites: Writes[SchoolRegistration] = (
    (__ \ "school_name").write[String] and
      (__ \ "school_address").write[Address]
    ) (unlift(SchoolRegistration.unapply))

  implicit val schoolJsonWrites: Writes[School] = (
    (__ \ "school_id").write[String] and
      (__ \ "school_name").write[String] and
      (__ \ "school_address").write[Address] and
      (__ \ "status").write[String]
    ) (unlift(School.unapply))

  implicit val schoolJsonReads: Reads[School] = (
    (__ \ "_id").read[String] and
      (__ \ "school_name").read[String] and
      (__ \ "school_address").read[Address] and
      (__ \ "status").read[String]
    ) (School.apply _)

  val registerSchoolForm = Form(schoolFormMapping)

  def findAll()(implicit ws: WSClient): Future[List[School]] = {
    Logger.debug(s"Get data for All schools")
    val json = Json.obj("query" ->
      """
        |FOR sc in schools FILTER sc.status != "D" RETURN sc
      """.stripMargin)
    GenericHelper.databaseCursor.post(json).map { res =>
      Logger.debug(s"The response data -> [${res.json}]")
      (res.json \ "result").validate[List[School]] match {
        case s: JsSuccess[List[School]] =>
          s.get.toList
        case e: JsError =>
          Logger.error(Json.prettyPrint(JsError.toJson(e)))
          Nil
      }
    }
  }

  //def findById(school: Long): Option[School] = schoolList.find(_.school_id == school)
  def findById(school_id: String)(implicit ws: WSClient): Future[Option[School]] = {
    Logger.debug(s"Get data for school with id [${school_id}]")
    GenericHelper.databaseDocumentApiRequestWithId(school_id)
      .get().map { res2 =>
      res2.json.validate[School] match {
        case s: JsSuccess[School] =>
          val school = s.get
          Some(school)
        case e: JsError =>
          Logger.error(Json.prettyPrint(JsError.toJson(e)))
          None
      }
    }
  }

  /**
    *
    * @param guardian_id
    * @return
    */
  def findAllSchoolsByGuardianId(guardian_id: Long) = GuardianHelper.findById(guardian_id) match {
    case Some(guardian) =>
      guardian.students.groupBy(_.student.school).map(_._1).toList
    case None => Nil
  }

  def addSchool(s: SchoolRegistration)(implicit ws: WSClient): Future[Option[String]] = {
    val json: JsValue = Json.toJson(s).as[JsObject] +("status", JsString(Reference.STATUS.ACTIVE))
    Logger.debug(s"Adding School -> ${Json.prettyPrint(json)}")
    GenericHelper.databaseGraphApiVertexRequest(Reference.DatabaseVertex.SCHOOL).post(json).map { res =>
      val school_id: String = (res.json \ "vertex" \ "_id").as[String]
      Logger.debug(s"Id of the School added -> ${school_id}")
      Some(school_id)
    }
  }
}

