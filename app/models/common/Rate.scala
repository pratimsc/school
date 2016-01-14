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

import models._
import models.common.time.{ONCE, Frequency, WEEKLY}
import org.joda.money.{CurrencyUnit, Money}
import org.joda.time._
import org.joda.time.format.ISODateTimeFormat

import scala.collection.immutable.List
import scala.collection.mutable.MutableList

/**
  * Created by pratimsc on 01/01/16.
  */
case class RateUnit(id: Long, minor: Hours, description: String)


object RateUnit {
  val perHour = RateUnit(1, Hours.hours(1), "Per Hour")
  val per8HourDay = RateUnit(2, Hours.hours(8), "Per Day (8 hours)")
  val per12HourDay = RateUnit(3, Hours.hours(12), "Per Day (12 hours)")
  val per30HoursWeek = RateUnit(1, Hours.hours(30), "Per Week (30 hours)")
}

trait Rate {
  def rate_id: Long;

  def code: String;

  def description: String;

  def chargeOrRebate: String;

  def status: String
}

case class FlatRate(rate_id: Long, code: String, description: String, chargeOrRebate: String, status: String, price: Money) extends Rate

case class BandedRate(rate_id: Long, code: String, description: String, chargeOrRebate: String, status: String, bands: List[Band], period: Option[ReadablePeriod]) extends Rate

case class Band(lower_limit: Option[Hours], upper_limit: Option[Hours], rate: Money) {
  require(lower_limit != None || upper_limit != None, "Both lower and upper limits can not be None.")
  require(lower_limit != upper_limit, "Lower limit should be same as the upper limit.")
}

case class RateAppliedToStudent(student: Student, rate: Rate, status: String, since: DateTime, until: Option[DateTime], count: Option[Long], frequency: Frequency)

object RateHelper {

  val ratesList: MutableList[Rate] = MutableList(
    FlatRate(1, "R01", "Base Rate", "Charge", "A", Money.ofMinor(CurrencyUnit.GBP, 900)),
    FlatRate(2, "R02", "Base Rate", "Charge", "A", Money.ofMinor(CurrencyUnit.GBP, 1100)),
    FlatRate(3, "R03", "Base Rate", "Charge", "A", Money.ofMinor(CurrencyUnit.GBP, 18000)),
    FlatRate(4, "R04", "Base Rate", "Charge", "A", Money.ofMinor(CurrencyUnit.GBP, 36000)),
    FlatRate(5, "R05", "Base Rate", "Rebate", "A", Money.ofMinor(CurrencyUnit.GBP, 450)),
    FlatRate(6, "R06", "Base Rate", "Rebate", "A", Money.ofMinor(CurrencyUnit.GBP, 550)),
    FlatRate(7, "R07", "Base Rate", "Rebate", "A", Money.ofMinor(CurrencyUnit.GBP, 9000)),
    FlatRate(8, "R08", "Base Rate", "Rebate", "A", Money.ofMinor(CurrencyUnit.GBP, 18000)),
    FlatRate(9, "R09", "Flat Rate 1", "Charge", "A", Money.ofMinor(CurrencyUnit.GBP, 1000)),
    FlatRate(10, "R10", "Flat Rate 2", "Charge", "A", Money.ofMinor(CurrencyUnit.GBP, 2000)),
    BandedRate(11, "R11", "Banded Rate 11", "Charge", "A", List(
      Band(Some(Hours.hours(1)), Some(Hours.hours(15)), Money.ofMinor(CurrencyUnit.GBP, 650)),
      Band(Some(Hours.hours(16)), Some(Hours.hours(30)), Money.ofMinor(CurrencyUnit.GBP, 1300)),
      Band(Some(Hours.hours(31)), None, Money.ofMinor(CurrencyUnit.GBP, 1500))),
      Some(Weeks.ONE)),
    BandedRate(12, "R11", "Banded Rate 11", "Charge", "A", List(
      Band(Some(Hours.hours(1)), Some(Hours.hours(15)), Money.ofMinor(CurrencyUnit.GBP, 750)),
      Band(Some(Hours.hours(16)), Some(Hours.hours(30)), Money.ofMinor(CurrencyUnit.GBP, 1500)),
      Band(Some(Hours.hours(31)), None, Money.ofMinor(CurrencyUnit.GBP, 1700))),
      Some(Weeks.ONE)),
    BandedRate(13, "R13", "Banded Rate 11", "Charge", "A", List(
      Band(Some(Hours.hours(1)), Some(Hours.hours(15)), Money.ofMinor(CurrencyUnit.GBP, 850)),
      Band(Some(Hours.hours(16)), Some(Hours.hours(30)), Money.ofMinor(CurrencyUnit.GBP, 1700)),
      Band(Some(Hours.hours(31)), None, Money.ofMinor(CurrencyUnit.GBP, 1900))),
      Some(Weeks.ONE))
  )

  val schoolRatesList: MutableList[(School, Rate)] = MutableList(
    (SchoolHelper.schoolList.get(0).get, ratesList.get(0).get),
    (SchoolHelper.schoolList.get(1).get, ratesList.get(1).get),
    (SchoolHelper.schoolList.get(0).get, ratesList.get(2).get),
    (SchoolHelper.schoolList.get(1).get, ratesList.get(3).get),
    (SchoolHelper.schoolList.get(0).get, ratesList.get(4).get),
    (SchoolHelper.schoolList.get(1).get, ratesList.get(5).get),
    (SchoolHelper.schoolList.get(0).get, ratesList.get(6).get),
    (SchoolHelper.schoolList.get(1).get, ratesList.get(7).get),
    (SchoolHelper.schoolList.get(0).get, ratesList.get(8).get),
    (SchoolHelper.schoolList.get(1).get, ratesList.get(9).get),
    (SchoolHelper.schoolList.get(1).get, ratesList.get(10).get),
    (SchoolHelper.schoolList.get(1).get, ratesList.get(11).get),
    (SchoolHelper.schoolList.get(1).get, ratesList.get(12).get)
  )

  val studentRatesList: MutableList[RateAppliedToStudent] = MutableList(
    RateAppliedToStudent(StudentHelper.studentList.get(0).get, schoolRatesList.get(0).get._2, "Active", DateTime.parse("20150101", ISODateTimeFormat.basicDate()), None, None, WEEKLY),
    RateAppliedToStudent(StudentHelper.studentList.get(1).get, schoolRatesList.get(2).get._2, "Active", DateTime.parse("20150101", ISODateTimeFormat.basicDate()), None, None, WEEKLY),
    RateAppliedToStudent(StudentHelper.studentList.get(2).get, schoolRatesList.get(1).get._2, "Active", DateTime.parse("20150101", ISODateTimeFormat.basicDate()), None, None, WEEKLY),
    RateAppliedToStudent(StudentHelper.studentList.get(3).get, schoolRatesList.get(1).get._2, "Active", DateTime.parse("20150101", ISODateTimeFormat.basicDate()), None, None, WEEKLY),
    RateAppliedToStudent(StudentHelper.studentList.get(4).get, schoolRatesList.get(3).get._2, "Active", DateTime.parse("20150101", ISODateTimeFormat.basicDate()), None, None, ONCE),
    RateAppliedToStudent(StudentHelper.studentList.get(4).get, schoolRatesList.get(9).get._2, "Active", DateTime.parse("20150101", ISODateTimeFormat.basicDate()), None, None, ONCE),
    RateAppliedToStudent(StudentHelper.studentList.get(4).get, schoolRatesList.get(10).get._2, "Active", DateTime.parse("20150101", ISODateTimeFormat.basicDate()), None, None, WEEKLY),
    RateAppliedToStudent(StudentHelper.studentList.get(4).get, schoolRatesList.get(11).get._2, "Active", DateTime.parse("20150101", ISODateTimeFormat.basicDate()), None, None, WEEKLY),
    RateAppliedToStudent(StudentHelper.studentList.get(4).get, schoolRatesList.get(12).get._2, "Active", DateTime.parse("20150101", ISODateTimeFormat.basicDate()), None, None, WEEKLY)
  )

  def findRateById(rate_id: Long) = ratesList.find(_.rate_id == rate_id)

  def findAllRatesBySchool(school_id: String): List[Rate] = schoolRatesList.filter(_._1.school_id == school_id).map(_._2).toList

  def findAllRatesByStudent(student_id: Long): List[Rate] = studentRatesList.filter(_.student.student_id == student_id).map(_.rate).toList

  def findAllAppliedRatesByStudent(student_id: Long): List[RateAppliedToStudent] = studentRatesList.filter(_.student.student_id == student_id).toList

  def findAllSchoolsByRate(rate_id: Long): List[School] = schoolRatesList.groupBy(_._1).map(_._1).toList

  def findAllStudentsByRate(rate_id: Long): List[Student] = studentRatesList.filter(_.rate.rate_id == rate_id).map(_.student).toList
}