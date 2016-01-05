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
import models.{SchoolHelper, Student, StudentHelper}
import org.joda.time.{DateTime, Days, Hours}

import scala.collection.immutable.Iterable
import scala.collection.mutable.MutableList

/**
  * Created by pratimsc on 03/01/16.
  */
//case class Timesheet()
case class DailyTimesheet(timesheet_id: Long, term_id: Long, student_id: Long, school_id: Long, date: DateTime, recordedHours: Hours, status: String)

case class WeeklyTimesheet(weekly_timesheet_id: Long, term_id: Long, student_id: Long, school_id: Long, startsOn: DateTime, endsOn: DateTime, recordedHours: List[DailyTimesheet], status: String)

object TimesheetHelper {

  private var daily_timesheet_unique_id_count: Long = 0

  private var weekly_timesheet_unique_id_count: Long = 0

  val dailyTimesheet: MutableList[DailyTimesheet] = MutableList()

  val weeklyTimesheetList: MutableList[WeeklyTimesheet] = MutableList()


  def findAllTimesheetsByStudent(student_id: Long): List[WeeklyTimesheet] = {
    weeklyTimesheetList.filter(_.student_id == student_id).toList
  }

  def findAllTimesheetsByTermAndSchool(term_id: Long, school_id: Long): List[WeeklyTimesheet] = {
    weeklyTimesheetList.toList
  }

  def populateTimesheet(t: Term, school_id: Long): List[DailyTimesheet] = {
    val duration = Days.daysBetween(t.begin.toLocalDate, t.finish.toLocalDate).getDays
    val school = SchoolHelper.findById(school_id)
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

  def purgeTimesheet(t: Term, school_id: Long): Boolean = {
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
    val updateWeeklyTimesheet: Iterable[WeeklyTimesheet] = dailyTimesheet.groupBy(dts => (dts.term_id, dts.student_id, dts.school_id)).map { e =>
      // WeeklyTimesheet(timesheet_id: Long, term_id: Long, student_id: Long, school_id: Long, startsOn: DateTime, endsOn: DateTime, recordedHours: List[DailyTimesheet], status: String)
      weekly_timesheet_unique_id_count = weekly_timesheet_unique_id_count + 1
      val dtsl = e._2.toList
      val status = "A"
      WeeklyTimesheet(weekly_timesheet_unique_id_count, e._1._1, e._1._2, e._1._3, dtsl.head.date, dtsl.last.date, dtsl, status)
    }.toList
    //Prepare weekly timesheet
    weeklyTimesheetList ++= updateWeeklyTimesheet
  }

}

