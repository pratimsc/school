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

import models.common.reference.Reference
import org.joda.time.DateTime
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
  * Created by pratimsc on 30/01/16.
  */
case class Holiday(holiday_id: String, description: String, date: DateTime, status: String)

case class HolidayRegistrationData(description: String, date: DateTime, status: String)

object HolidayHelper {

  private val holidayFormMapping = mapping(
    "description" -> text,
    "date" -> jodaDate("yyyy-MM-dd"),
    "status" -> text
  )(HolidayRegistrationData.apply)(HolidayRegistrationData.unapply)

  val registerHolidayForm = Form(holidayFormMapping)

  implicit val holidayRegJsonWrite: Writes[HolidayRegistrationData] = (
    (__ \ "description").write[String] and
      (__ \ "date").write[DateTime] and
      (__ \ "status").write[String]
    ) (unlift(HolidayRegistrationData.unapply))

  implicit val holidayJsonWrite: Writes[Holiday] = (
    (__ \ "_id").write[String] and
      (__ \ "description").write[String] and
      (__ \ "date").write[DateTime] and
      (__ \ "status").write[String]
    ) (unlift(Holiday.unapply))

  implicit val holidayJsonRead: Reads[Holiday] = (
    (__ \ "_id").read[String] and
      (__ \ "description").read[String] and
      (__ \ "date").read[DateTime] and
      (__ \ "status").read[String]
    ) (Holiday.apply _)

  def findAllBySchool(school_id: String)(implicit ws: WSClient): Future[List[Holiday]] = {
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |filter sc._id == "${school_id}" && sc.status != "${Reference.Status.DELETE}"
         |FOR e in ${DBEdges.SCHOOL_HAS_HOLIDAY}
         |filter e._from == sc._id
         |FOR hl in ${DBDocuments.HOLIDAYS}
         |filter hl._id == e._to && hl.status != "${Reference.Status.DELETE}"
         |return hl
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the hlidays based on the school id [${school_id}] is -> ${res.json}")
      val holidays: List[Holiday] = (res.json \ "result").as[List[Holiday]]
      holidays
    }
  }

  def findByIdAndSchool(holiday_id: String, school_id: String)(implicit ws: WSClient): Future[Option[Holiday]] = {
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |filter sc._id == "${school_id}" && sc.status != "${Reference.Status.DELETE}"
         |FOR e in ${DBEdges.SCHOOL_HAS_HOLIDAY}
         |filter e._from == sc._id
         |FOR hl in ${DBDocuments.HOLIDAYS}
         |filter hl._id == e._to && hl._id == "${holiday_id}"  && hl.status != "${Reference.Status.DELETE}"
         |return hl
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the holiday based on the id [${holiday_id}] is -> ${res.json}")
      val students: List[Holiday] = (res.json \ "result").as[List[Holiday]]
      students match {
        case h :: t =>
          Logger.debug(s"Details of the holiday extracted ${h}")
          Some(h)
        case _ => None
      }
    }
  }

  def addHoliday(h: HolidayRegistrationData, school_id: String)(implicit ws: WSClient): Future[Some[String]] = {
    val h_json: JsValue = Json.toJson(h).as[JsObject]
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |  FILTER sc._id == "${school_id}" && sc.status != "${Reference.Status.DELETE}"
         |  INSERT ${h_json} in ${DBDocuments.HOLIDAYS}
         |  let hl = NEW
         |  INSERT {
         |  "_from":sc._id,
         |  "_to":hl._id
         |  }in ${DBEdges.SCHOOL_HAS_HOLIDAY}
         |  let rel = NEW
         |return {"holiday":hl, "school":sc, "relation":rel}
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the holiday added ->${Json.prettyPrint(res.json)}")
      val result = (res.json \ "result") (0)
      val sc_json = result \ "school"
      val hl_json = result \ "holiday"
      val holiday_id = (hl_json \ "_id").as[String]
      Some(holiday_id)
    }
  }
}