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
import play.api.libs.ws.WSClient

import scala.collection.immutable.Iterable
import scala.collection.mutable.MutableList

/**
  * Created by pratimsc on 03/01/16.
  */
case class DailyTimesheet(timesheet_id: String, date: DateTime, recordedHours: Hours, status: String)

case class WeeklyTimesheet(weekly_timesheet_id: Long, term_id: Long, student_id: String, school_id: String, startsOn: DateTime, endsOn: DateTime, recordedHours: List[DailyTimesheet], status: String)

object TimesheetHelper {

  private var daily_timesheet_unique_id_count: Long = 0

  private var weekly_timesheet_unique_id_count: Long = 0

  val dailyTimesheet: MutableList[DailyTimesheet] = MutableList()

  val weeklyTimesheetList: MutableList[WeeklyTimesheet] = MutableList()

  def findByIdAndSchool(timesheet_id: Long, school_id: String): Option[WeeklyTimesheet] = {
    weeklyTimesheetList.find(e => (e.school_id == school_id && e.weekly_timesheet_id == timesheet_id))
  }

  def findAllTimesheetsByStudent(student_id: String): List[WeeklyTimesheet] = {
    weeklyTimesheetList.filter(_.student_id == student_id).toList
  }

  def findAllTimesheetsByTermAndSchool(term_id: Long, school_id: String): List[WeeklyTimesheet] = {
    weeklyTimesheetList.filter(e => (e.school_id == school_id && e.term_id == term_id)).toList
  }

  def findAllTimesheetsBySchool(school_id: String): List[WeeklyTimesheet] = {
    weeklyTimesheetList.filter(_.school_id == school_id).toList
  }

  def populateTimesheet(t: Term, school_id: String)(implicit ws: WSClient): List[DailyTimesheet] = ???


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

