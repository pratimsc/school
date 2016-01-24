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
import org.maikalal.common.util.ArangodbDatabaseUtility
import org.maikalal.common.util.ArangodbDatabaseUtility.{DBDocuments, DBEdges}
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
    "name" -> nonEmptyText(minLength = 10, maxLength = Char.MaxValue),
    "address" -> AddressHelper.addressFormMapping
  )(SchoolRegistration.apply)(SchoolRegistration.unapply)

  val registerSchoolForm = Form(schoolFormMapping)

  implicit val schoolRegDataJsonWrites: Writes[SchoolRegistration] = (
    (__ \ "name").write[String] and
      (__ \ "address").write[Address]
    ) (unlift(SchoolRegistration.unapply))

  implicit val schoolJsonWrites: Writes[School] = (
    (__ \ "_id").write[String] and
      (__ \ "name").write[String] and
      (__ \ "address").write[Address] and
      (__ \ "status").write[String]
    ) (unlift(School.unapply))

  implicit val schoolJsonReads: Reads[School] = (
    (__ \ "_id").read[String] and
      (__ \ "name").read[String] and
      (__ \ "address").read[Address] and
      (__ \ "status").read[String]
    ) (School.apply _)


  def findAll()(implicit ws: WSClient): Future[List[School]] = {
    Logger.debug(s"Get data for All schools")
    val json = Json.obj("query" ->
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS} FILTER sc.status != "D" RETURN sc
      """.stripMargin)
    Logger.debug(s"Cursor Query ->\n [${json}]")
    ArangodbDatabaseUtility.databaseCursor.post(json).map { res =>
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

  def findById(school_id: String)(implicit ws: WSClient): Future[Option[School]] = {
    Logger.debug(s"Get data for school with id [${school_id}]")
    ArangodbDatabaseUtility.databaseDocumentApiRequestWithId(school_id)
      .get().map { res2 =>
      Logger.debug(s"Detail of school with id [${school_id}] ->${res2.json}")
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
  def findAllSchoolsByGuardianId(guardian_id: String)(implicit ws: WSClient): Future[List[School]] = {
    val aql =
      s"""
         |FOR gu in ${DBDocuments.GUARDIANS}
         |filter gu._id == "${guardian_id}"  && gu.status != "${Reference.STATUS.DELETE}"
         |FOR e in ${DBDocuments.SCHOOLS}
         |filter e._to == gu._id
         |FOR sc in ${DBEdges.SCHOOL_DEALS_WITH_GUARDIAN}
         |filter sc._id == e._from && sc.status != "${Reference.STATUS.DELETE}"
         |return sc
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the schools based on the id [${guardian_id}] is -> ${res.json}")
      val schools: List[School] = (res.json \ "result").as[List[School]]
      schools
    }
  }

  def addSchool(s: SchoolRegistration)(implicit ws: WSClient): Future[Option[String]] = {
    val sc_json: JsValue = Json.toJson(s).as[JsObject] +("status", JsString(Reference.STATUS.ACTIVE))
    val aql =
      s"""
         |INSERT ${sc_json} in ${DBDocuments.SCHOOLS}
         |  LET sc = NEW
         |RETURN sc
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the school added ->${Json.prettyPrint(res.json)}")
      val school = (res.json \ "result") (0)
      val school_id = (school \ "_id").as[String]
      Logger.debug(s"Id of the School added -> ${school_id}")
      Some(school_id)
    }
  }
}

