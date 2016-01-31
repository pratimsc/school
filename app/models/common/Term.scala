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
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.Future


/**
  * Created by pratimsc on 04/01/16.
  */
case class Term(term_id: String, begin: DateTime, finish: DateTime, status: String, timesheet: String, invoice: String)

case class TermRegistrationData(begin: DateTime, finish: DateTime)

object TermHelper {

  private val termFormMaping = mapping(
    "begin_date" -> jodaDate("yyyy-MM-dd"),
    "finish_date" -> jodaDate("yyyy-MM-dd")
  )(TermRegistrationData.apply)(TermRegistrationData.unapply)

  val registerTermForm = Form(termFormMaping)

  implicit val registerTermJsonWrite: Writes[TermRegistrationData] = (
    (__ \ "begin_date").write[DateTime] and
      (__ \ "finish_date").write[DateTime]
    ) (unlift(TermRegistrationData.unapply))

  implicit val termJsonWrite: Writes[Term] = (
    (__ \ "_id").write[String] and
      (__ \ "begin_date").write[DateTime] and
      (__ \ "finish_date").write[DateTime] and
      (__ \ "status").write[String] and
      (__ \ "timesheet").write[String] and
      (__ \ "invoice").write[String]
    ) (unlift(Term.unapply))
  implicit val termJsonRead: Reads[Term] = (
    (__ \ "_id").read[String] and
      (__ \ "begin_date").read[DateTime] and
      (__ \ "finish_date").read[DateTime] and
      (__ \ "status").read[String] and
      (__ \ "timesheet").read[String] and
      (__ \ "invoice").read[String]
    ) (Term.apply _)

  def findAllBySchool(school_id: String)(implicit ws: WSClient): Future[List[Term]] = {
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |filter sc._id == "${school_id}" && sc.status != "${Reference.Status.DELETE}"
         |FOR e in ${DBEdges.SCHOOL_HAS_TERM}
         |filter e._from == sc._id
         |FOR tr in ${DBDocuments.TERMS}
         |filter tr._id == e._to && tr.status != "${Reference.Status.DELETE}"
         |return tr
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the terms based on the school id [${school_id}] is -> ${res.json}")
      val terms: List[Term] = (res.json \ "result").as[List[Term]]
      terms
    }
  }

  def findById(term_id: String, school_id: String)(implicit ws: WSClient): Future[Option[Term]] = {
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |filter sc._id == "${school_id}" && sc.status != "${Reference.Status.DELETE}"
         |FOR e in ${DBEdges.SCHOOL_HAS_TERM}
         |filter e._from == sc._id
         |FOR tr in ${DBDocuments.TERMS}
         |filter tr._id == e._to && tr._id== "${term_id}" && tr.status != "${Reference.Status.DELETE}"
         |return tr
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the terms based on the term id [${term_id}] and school id [${school_id}] is -> ${res.json}")
      (res.json \ "result").as[List[Term]] match {
        case h :: t => Some(h)
        case Nil => None
      }
    }
  }

  def addTerm(trd: TermRegistrationData, school_id: String)(implicit ws: WSClient): Future[Option[String]] = {
    val t_json: JsValue = Json.toJson(trd).as[JsObject] +
      ("status", JsString(Reference.Status.ACTIVE)) +
      ("timesheet", JsString(Reference.Term.Timesheet.ABSENT)) +
      ("invoice", JsString(Reference.Term.Invoice.PENDING))
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |  FILTER sc._id == "${school_id}" && sc.status != "${Reference.Status.DELETE}"
         |  INSERT ${t_json} in ${DBDocuments.TERMS}
         |  let trm = NEW
         |  INSERT {
         |  "_from":sc._id,
         |  "_to":trm._id
         |  }in ${DBEdges.SCHOOL_HAS_TERM}
         |  let rel = NEW
         |return {"term":trm, "school":sc, "relation":rel}
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the term added ->${Json.prettyPrint(res.json)}")
      val result = (res.json \ "result") (0)
      val sc_json = result \ "school"
      val st_json = result \ "term"
      val term_id = (st_json \ "_id").as[String]
      Some(term_id)
    }
  }


  def purgeById(term_id: Long, school_id: String)(implicit ws: WSClient): Boolean = ???

}

