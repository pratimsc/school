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

import org.joda.time.{DateTime, Hours}
import org.maikalal.common.util.DateUtility.{hourJsonReads, hourJsonWrites}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.collection.immutable.Iterable
import scala.concurrent.Future

/**
  * Created by pratimsc on 03/01/16.
  */
case class DailyTimesheet(timesheet_id: String, date: DateTime, recordedHours: Hours, status: String, timesheet_status: String)

case class WeeklyTimesheet(weekly_timesheet_id: Long, term_id: Long, student_id: String, school_id: String, startsOn: DateTime, endsOn: DateTime, recordedHours: List[DailyTimesheet], status: String)

object TimesheetHelper {

  implicit lazy val dailyTimesheetReadJson: Reads[DailyTimesheet] = (
    (__ \ "_id").read[String] and
      (__ \ "date").read[DateTime] and
      (__ \ "recordedHours").read[Hours] and
      (__ \ "status").read[String] and
      (__ \ "timesheet").read[String]
    ) (DailyTimesheet.apply _)

  implicit lazy val dailyTimesheetWriteJson: Writes[DailyTimesheet] = (
    (__ \ "_id").write[String] and
      (__ \ "date").write[DateTime] and
      (__ \ "recordedHours").write[Hours] and
      (__ \ "status").write[String] and
      (__ \ "timesheet").write[String]
    ) (unlift(DailyTimesheet.unapply))


  def findByIdAndSchool(timesheet_id: String, school_id: String)(implicit ws: WSClient): Future[Option[WeeklyTimesheet]] = ???

  def findAllTimesheetsByStudent(student_id: String)(implicit ws: WSClient): Future[List[WeeklyTimesheet]] = ???

  def findAllTimesheetsByTermAndSchool(term_id: String, school_id: String)(implicit ws: WSClient): Future[List[WeeklyTimesheet]] = ???

  def findAllTimesheetsBySchool(school_id: String)(implicit ws: WSClient): Future[List[WeeklyTimesheet]] = ???

  def populateTimesheet(term_id: String, school_id: String)(implicit ws: WSClient): List[DailyTimesheet] = ???


  def purgeTimesheet(t: Term, school_id: String): Boolean = ???


  private def prepareWeeklyTimesheetFromDaily(): Unit = ???

  /*{
  weeklyTimesheetList.clear()
  weekly_timesheet_unique_id_count = 0
  val groupedDailyTimesheets: Iterable[(Long, String, String, List[DailyTimesheet])] = dailyTimesheet.groupBy(dts => (dts.term_id, dts.student_id, dts.school_id)).map { e =>
    val dtsl = e._2.toList
    val status = "A"
    val splitTo7Days: List[List[DailyTimesheet]] = dtsl.foldRight(List[List[DailyTimesheet]]()) { (d, wl) =>
      wl match {
        case Nil => List(List(d))
        case h :: t => {
          h.length match {
            case 7 => List(d) :: wl
            case _ => (d :: h) :: t
          }
        }
      }
    }
    splitTo7Days.map((e._1._1, e._1._2, e._1._3, _))
  }.flatten
*/
  // WeeklyTimesheet(timesheet_id: Long, term_id: Long, student_id: Long, school_id: Long, startsOn: DateTime, endsOn: DateTime, recordedHours: List[DailyTimesheet], status: String)

  val updateWeeklyTimesheet: Iterable[WeeklyTimesheet] = ???
  /*groupedDailyTimesheets.map { e =>
      weekly_timesheet_unique_id_count = weekly_timesheet_unique_id_count + 1
      WeeklyTimesheet(weekly_timesheet_unique_id_count, e._1, e._2, e._3, e._4.head.date, e._4.last.date,
        e._4.sortWith((d1, d2) => d1.date.toLocalDate.isBefore(d2.date.toLocalDate)), Reference.Status.ACTIVE)
    }
    //Prepare weekly timesheet
    weeklyTimesheetList ++= updateWeeklyTimesheet
  }*/

}

