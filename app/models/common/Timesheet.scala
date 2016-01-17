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
import models.{SchoolHelper, StudentHelper}
import org.joda.time.{DateTime, Days, Hours}
import play.api.libs.ws.WSClient

import scala.collection.immutable.Iterable
import scala.collection.mutable.MutableList
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by pratimsc on 03/01/16.
  */
//case class Timesheet()
case class DailyTimesheet(timesheet_id: Long, term_id: Long, student_id: Long, school_id: String, date: DateTime, recordedHours: Hours, status: String)

case class WeeklyTimesheet(weekly_timesheet_id: Long, term_id: Long, student_id: Long, school_id: String, startsOn: DateTime, endsOn: DateTime, recordedHours: List[DailyTimesheet], status: String)

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

  def populateTimesheet(t: Term, school_id: String)(implicit ws: WSClient): List[DailyTimesheet] = {

    //Add 1 to include the finish date in the calendar also
    val school = Await.result(SchoolHelper.findById(school_id), Duration.Inf)
    val duration: Int = Days.daysBetween(t.begin.toLocalDate, t.finish.toLocalDate).getDays + 1
    val students = StudentHelper.findAll(school_id)
    val dtsl: List[DailyTimesheet] = school match {
      case Some(sc) => for {
        st <- students
        day <- 0 until duration
      } yield {
        daily_timesheet_unique_id_count = daily_timesheet_unique_id_count + 1
        val dts = DailyTimesheet(daily_timesheet_unique_id_count, t.term_id, st.student_id, sc.school_id, t.begin.plusDays(day), Reference.DAILY_TIMESHEET_INITIAL_HOURS, Reference.STATUS.ACTIVE)
        dts
      }
      case None => Nil
    }
    //Add all time sheet to existing one.
    dailyTimesheet ++= dtsl
    //Prepare weekly timesheet
    prepareWeeklyTimesheetFromDaily
    dailyTimesheet.filter(_.term_id == t.term_id).toList
  }

  def purgeTimesheet(t: Term, school_id: String): Boolean = {
    val updatedTimesheets = dailyTimesheet.map { dts =>
      if (dts.term_id == t.term_id && dts.school_id == school_id) dts.copy(status = Reference.STATUS.DELETE)
      else dts
    }
    dailyTimesheet.clear()
    dailyTimesheet ++= updatedTimesheets
    //Prepare weekly timesheet
    prepareWeeklyTimesheetFromDaily
    updatedTimesheets.size match {
      case 0 => false
      case _ => true
    }
  }

  private def prepareWeeklyTimesheetFromDaily(): Unit = {
    weeklyTimesheetList.clear()
    weekly_timesheet_unique_id_count = 0
    val groupedDailyTimesheets: Iterable[(Long, Long, String, List[DailyTimesheet])] = dailyTimesheet.groupBy(dts => (dts.term_id, dts.student_id, dts.school_id)).map { e =>
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

    // WeeklyTimesheet(timesheet_id: Long, term_id: Long, student_id: Long, school_id: Long, startsOn: DateTime, endsOn: DateTime, recordedHours: List[DailyTimesheet], status: String)

    val updateWeeklyTimesheet: Iterable[WeeklyTimesheet] = groupedDailyTimesheets.map { e =>
      weekly_timesheet_unique_id_count = weekly_timesheet_unique_id_count + 1
      WeeklyTimesheet(weekly_timesheet_unique_id_count, e._1, e._2, e._3, e._4.head.date, e._4.last.date,
        e._4.sortWith((d1, d2) => d1.date.toLocalDate.isBefore(d2.date.toLocalDate)), Reference.STATUS.ACTIVE)
    }
    //Prepare weekly timesheet
    weeklyTimesheetList ++= updateWeeklyTimesheet
  }

}

