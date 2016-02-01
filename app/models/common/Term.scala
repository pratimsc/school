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

import models.common.TimesheetHelper.dailyTimesheetReadJson
import models.common.reference.Reference
import models.{School, SchoolHelper, Student, StudentHelper}
import org.joda.time.{DateTime, Hours}
import org.maikalal.common.util.ArangodbDatabaseUtility.{DBDocuments, DBEdges}
import org.maikalal.common.util.DateUtility.hourJsonWrites
import org.maikalal.common.util.{ArangodbDatabaseUtility, DateUtility}
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
case class Term(term_id: String, begin: DateTime, finish: DateTime, status: String, timesheet_status: String, invoice_status: String)

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

  def findByIdAndSchool(term_id: String, school_id: String)(implicit ws: WSClient): Future[Option[Term]] = {
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

  /**
    *
    * @param term_id
    * @param school_id
    * @param ws
    * @return
    */
  def generateTimesheets(term_id: String, school_id: String)(implicit ws: WSClient): Future[List[DailyTimesheet]] = {
    val sc: Future[Option[School]] = SchoolHelper.findById(school_id)
    val st: Future[List[Student]] = StudentHelper.findAllBySchool(school_id)
    val tr: Future[Option[Term]] = TermHelper.findByIdAndSchool(term_id, school_id)
    val hl: Future[List[Holiday]] = HolidayHelper.findAllBySchool(school_id)

    val ds: Future[Future[List[DailyTimesheet]]] = for {
      school <- sc
      term <- tr
      students <- st
      holidays <- hl
    } yield {
      term match {
        case Some(t) =>
          val tsl: Future[List[DailyTimesheet]] = Future.sequence(createTimesheetForStudentsInTerm(students, t, holidays.map(_.date)))
          tsl
        case None =>
          Future.successful(Nil)
      }
    }
    ds.flatMap(f => f)
  }

  /**
    *
    * @param students
    * @param term
    * @param holidays
    * @param ws
    * @return
    */
  private def createTimesheetForStudentsInTerm(students: List[Student], term: Term, holidays: List[DateTime])(implicit ws: WSClient): List[Future[DailyTimesheet]] = {
    for {
      date <- DateUtility.datesBetween(term.begin, term.finish)
      student <- students
    } yield
      createDailyTimesheetForStudentInTerm(term.term_id, student.student_id, date, holidays)
  }

  /**
    *
    * @param term_id
    * @param student_id
    * @param date
    * @param holidays
    * @param ws
    * @return
    */
  private def createDailyTimesheetForStudentInTerm(term_id: String, student_id: String, date: DateTime, holidays: List[DateTime])(implicit ws: WSClient): Future[DailyTimesheet] = {
    val dt_json = Json.obj(
      "date" -> date,
      "recordedHours" -> Hours.ZERO,
      "status" -> Reference.Status.ACTIVE,
      "timesheet" -> {
        if (DateUtility.dateIsWeekend(date) || DateUtility.dateIsHoliday(date, holidays))
          Reference.DailyTimesheet.Timesheet.SUBMITED
        else
          Reference.DailyTimesheet.Timesheet.CREATED
      }
    )
    val aql =
      s"""
         |INSERT ${dt_json} in ${DBDocuments.TIMESHEETS_DAILY}
         |LET tsd = NEW
         |INSERT{ "_from":${term_id}, "_to":tsd._id } IN ${DBEdges.TERM_HAS_TIMESHEET}
         |INSERT{ "_from":${student_id}, "_to":tsd._id } IN ${DBEdges.STUDENT_HAS_TIMESHEET}
         |RETURN {"dailyTimesheet":tsd}
        """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.
      aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      val result: JsLookupResult = (res.json \ "result") (0)
      val ts: DailyTimesheet = (result \ "dailyTimesheet").as[DailyTimesheet]
      ts
    }
  }

  def purgeById(term_id: Long, school_id: String)(implicit ws: WSClient): Boolean = ???

}

