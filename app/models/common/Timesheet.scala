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
import org.joda.time.{DateTime, Hours}
import org.maikalal.common.util.ArangodbDatabaseUtility
import org.maikalal.common.util.ArangodbDatabaseUtility.{DBDocuments, DBEdges}
import org.maikalal.common.util.DateUtility.{hourJsonReads, hourJsonWrites}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.Future

/**
  * Created by pratimsc on 03/01/16.
  */
case class DailyTimesheet(timesheet_id: String, date: DateTime, recordedHours: Hours, status: String, timesheet_status: String)

case class WeeklyTimesheet(week: Int, year: Int, startsOn: DateTime, endsOn: DateTime, recordedHours: Map[Int, DailyTimesheet], status: String)

object TimesheetHelper {

  implicit val dailyTimesheetReadJson: Reads[DailyTimesheet] = (
    (__ \ "_id").read[String] and
      (__ \ "date").read[DateTime] and
      (__ \ "recordedHours").read[Hours] and
      (__ \ "status").read[String] and
      (__ \ "timesheet").read[String]
    ) (DailyTimesheet.apply _)

  implicit val dailyTimesheetWriteJson: Writes[DailyTimesheet] = (
    (__ \ "_id").write[String] and
      (__ \ "date").write[DateTime] and
      (__ \ "recordedHours").write[Hours] and
      (__ \ "status").write[String] and
      (__ \ "timesheet").write[String]
    ) (unlift(DailyTimesheet.unapply))


  def findByIdAndSchool(timesheet_id: String, school_id: String)(implicit ws: WSClient): Future[Option[DailyTimesheet]] = {
    val aql =
      s"""
         |FOR dts in ${DBDocuments.TIMESHEETS_DAILY}
         |filter dts._id == "${timesheet_id}" && dts.status != "${Reference.Status.DELETE}"
         |return dts
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Daily Timesheet based on the timesheet id [${timesheet_id}] is -> ${res.json}")
      (res.json \ "result").as[List[DailyTimesheet]] match {
        case h :: t => Some(h)
        case Nil => None
      }
    }
  }

  def findAllTimesheetsByStudent(student_id: String)(implicit ws: WSClient): Future[List[DailyTimesheet]] = {
    val aql =
      s"""
         |FOR st in ${DBDocuments.STUDENTS}
         |filter st._id == "${student_id}" && st.status != "${Reference.Status.DELETE}"
         |FOR e1 in ${DBEdges.STUDENT_HAS_TIMESHEET}
         |filter e1._from == st._id
         |FOR dts in ${DBDocuments.TIMESHEETS_DAILY}
         |filter dts._id == e1._to && dts.status != "${Reference.Status.DELETE}"
         |return dts
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the timesheets based on the student id [${student_id}] is -> ${res.json}")
      val dailyTimesheets: List[DailyTimesheet] = (res.json \ "result").as[List[DailyTimesheet]]
      dailyTimesheets
    }
  }

  def findAllTimesheetsBySchool(school_id: String)(implicit ws: WSClient): Future[List[DailyTimesheet]] = {
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |FILTER sc._id ==  "${school_id}" && sc.status != "${Reference.Status.DELETE}"
         |FOR e2 in ${DBEdges.SCHOOL_ENROLLED_STUDENT}
         |FILTER e2._from == sc._id
         |FOR st in ${DBDocuments.STUDENTS}
         |filter st._id == e2._to && st.status != "${Reference.Status.DELETE}"
         |FOR e1 in ${DBEdges.STUDENT_HAS_TIMESHEET}
         |filter e1._from == st._id
         |FOR dts in ${DBDocuments.TIMESHEETS_DAILY}
         |filter dts._id == e1._to && dts.status != "${Reference.Status.DELETE}"
         |return dts
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the timesheets based on the school id [${school_id}] is -> ${res.json}")
      val dailyTimesheets: List[DailyTimesheet] = (res.json \ "result").as[List[DailyTimesheet]]
      dailyTimesheets
    }
  }

  def findAllTimesheetsByTermAndSchool(term_id: String, school_id: String)(implicit ws: WSClient): Future[List[DailyTimesheet]] = {
    val aql =
      s"""
         |FOR sc in ${DBDocuments.SCHOOLS}
         |FILTER sc._id ==  "${school_id}" && sc.status != "${Reference.Status.DELETE}"
         |FOR e2 in ${DBEdges.SCHOOL_HAS_TERM}
         |FILTER e2._from == sc._id
         |FOR tr in ${DBDocuments.TERMS}
         |filter tr._id == e2._to  && tr._id == "${term_id}" && tr.status != "${Reference.Status.DELETE}"
         |FOR e1 in ${DBEdges.TERM_HAS_TIMESHEET}
         |filter e1._from == tr._id
         |FOR dts in ${DBDocuments.TIMESHEETS_DAILY}
         |filter dts._id == e1._to && dts.status != "${Reference.Status.DELETE}"
         |return dts
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the timesheets based on the term id [${term_id}] and school id [${school_id}] is -> ${res.json}")
      val dailyTimesheets: List[DailyTimesheet] = (res.json \ "result").as[List[DailyTimesheet]]
      dailyTimesheets
    }
  }

  def convertDailyTimesheetsToWeeklyTimesheets(dl: List[DailyTimesheet]): List[WeeklyTimesheet] = {
    val dl1: List[(Int, Int, List[DailyTimesheet])] = dl.map(d => (d.date.getWeekOfWeekyear, d.date.getWeekyear, d))
      .groupBy(t => (t._1, t._2))
      .map(e => (e._1, e._2.map(_._3).sortWith((e1, e2) => e1.date.toLocalDate.isBefore(e2.date.toLocalDate)))).toList.map(e => (e._1._1, e._1._2, e._2))

    val wl = dl1.map { e =>
      //Weekly timesheet is submitted only if all the Daily timesheets under it are in submit status
      val weekStatus: String = {
        if (e._3.filter(_.status == Reference.DailyTimesheet.Timesheet.SUBMITED).length == e._3.length)
          Reference.DailyTimesheet.Timesheet.SUBMITED
        else
          Reference.DailyTimesheet.Timesheet.CREATED
      }
      val weeklyRecordedHours: Map[Int, DailyTimesheet] = Map(e._3.map(e => (e.date.getDayOfWeek, e)): _*)
      WeeklyTimesheet(week = e._1, year = e._2, startsOn = e._3.head.date, endsOn = e._3.last.date, recordedHours = weeklyRecordedHours, status = weekStatus)
    }
    wl.sortWith((w1, w2) => (w1.year < w2.year) || (w1.year == w2.year && w1.week < w2.week))
  }


}

