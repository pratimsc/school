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

import models.{StudentHelper, Student}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, Hours}

import scala.collection.mutable

/**
  * Created by pratimsc on 03/01/16.
  */
//case class Timesheet()
case class DailyTimesheet(timesheet_id: Long, date: DateTime, recordedHours: Hours, status: String)

case class WeeklyTimesheet(timesheet_id: Long, startsOn: DateTime, endsOn: DateTime, recordedHours: List[DailyTimesheet], status: String)

case class StudentWeeklyTimesheet(student: Student, timesheets: List[WeeklyTimesheet])

object TimesheetHelper {
  val weeklyTimesheetList = scala.collection.mutable.MutableList(
    WeeklyTimesheet(1, DateTime.parse("20151228", ISODateTimeFormat.basicDate), DateTime.parse("20160103", ISODateTimeFormat.basicDate),
      List(1, 2, 3, 4, 5, 6, 7).map(d => DailyTimesheet(1 + d, DateTime.parse("20151227", ISODateTimeFormat.basicDate).plusDays(1), Hours.hours(5), "S")),
      "E"),
    WeeklyTimesheet(2, DateTime.parse("20160104", ISODateTimeFormat.basicDate), DateTime.parse("20160110", ISODateTimeFormat.basicDate),
      List(1, 2, 3, 4, 5, 6, 7).map(d => DailyTimesheet(7 + d, DateTime.parse("20160103", ISODateTimeFormat.basicDate).plusDays(1), Hours.hours(5), "S"))
      , "S"),
    WeeklyTimesheet(3, DateTime.parse("20160111", ISODateTimeFormat.basicDate), DateTime.parse("20160117", ISODateTimeFormat.basicDate),
      List(1, 2, 3, 4, 5, 6, 7).map(d => DailyTimesheet(14 + d, DateTime.parse("20160110", ISODateTimeFormat.basicDate).plusDays(1), Hours.hours(5), "S"))
      , "E"),
    WeeklyTimesheet(4, DateTime.parse("20160118", ISODateTimeFormat.basicDate), DateTime.parse("20160124", ISODateTimeFormat.basicDate),
      List(1, 2, 3, 4, 5, 6, 7).map(d => DailyTimesheet(21 + d, DateTime.parse("20160117", ISODateTimeFormat.basicDate).plusDays(1), Hours.hours(5), "S"))
      , "E")
  )

  val studentWeeklyTimesheetList: mutable.MutableList[StudentWeeklyTimesheet] = StudentHelper.studentList.map(s => StudentWeeklyTimesheet(s, weeklyTimesheetList.toList))


  def findAllTimesheetsByStudent(student_id: Long) = {
    studentWeeklyTimesheetList.find(_.student.student_id == student_id) match {
      case Some(r) => r.timesheets
      case None => Nil
    }
  }

}

