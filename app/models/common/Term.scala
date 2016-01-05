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

import models.SchoolHelper
import models.common.reference.Reference
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.data.Form
import play.api.data.Forms._

import scala.collection.mutable.MutableList


/**
  * Created by pratimsc on 04/01/16.
  */
case class Term(term_id: Long, school_id: Long, begin: DateTime, finish: DateTime, status: String)

case class TermRegistrationData(begin: DateTime, finish: DateTime)

object TermHelper {

  private val initialDate = DateTime.parse("20160104", ISODateTimeFormat.basicDate)
  var term_unique_id_count = 6
  val termList = MutableList(
    Term(1, SchoolHelper.schoolList.get(0).get.school_id, initialDate, initialDate.plusWeeks(12).plusDays(6), "A"),
    Term(2, SchoolHelper.schoolList.get(0).get.school_id, initialDate.plusWeeks(13), initialDate.plusWeeks(13 + 12).plusDays(6), "A"),
    Term(3, SchoolHelper.schoolList.get(0).get.school_id, initialDate.plusWeeks(13 + 13), initialDate.plusWeeks(13 + 13 + 12).plusDays(6), "A"),
    Term(4, SchoolHelper.schoolList.get(1).get.school_id, initialDate, initialDate.plusWeeks(12).plusDays(6), "A"),
    Term(5, SchoolHelper.schoolList.get(1).get.school_id, initialDate.plusWeeks(13), initialDate.plusWeeks(13 + 12).plusDays(6), "A"),
    Term(6, SchoolHelper.schoolList.get(1).get.school_id, initialDate.plusWeeks(13 + 13), initialDate.plusWeeks(13 + 13 + 12).plusDays(6), "A")
  )
  //nitialize all termsheets
  val tsl = termList.map(t => TimesheetHelper.populateTimesheet(t, t.school_id))

  //val schoolTermList: scala.collection.mutable.Map[Long, MutableList[Term]] = scala.collection.mutable.Map((SchoolHelper.schoolList.get(0).get.school_id -> termList),
  // (SchoolHelper.schoolList.get(1).get.school_id -> termList.map(t => t.copy(term_id = (t.term_id + termList.head.term_id)))))

  private val termFormMaping = mapping(
    "begin_date" -> jodaDate("yyyy-MM-dd"),
    "finish_date" -> jodaDate("yyyy-MM-dd")
  )(TermRegistrationData.apply)(TermRegistrationData.unapply)

  val registerTermForm = Form(termFormMaping)


  def findAllBySchool(school_id: Long): List[Term] = termList.filter(_.school_id == school_id).toList

  def findById(term_id: Long, school_id: Long): Option[Term] = termList.find(t => (t.term_id == term_id && t.school_id == school_id))

  def addTerm(trd: TermRegistrationData, school_id: Long): Option[Term] = SchoolHelper.findById(school_id) match {
    case None => None
    case Some(school) =>

      val term = Term(termList.last.term_id + 1, school.school_id, trd.begin, trd.finish, "A")
      termList += term
      TimesheetHelper.populateTimesheet(term, school_id)
      Some(term)
  }


  def purgeById(term_id: Long, school_id: Long): Boolean = {
    term_unique_id_count = 0

    val updatedTermList = termList.map { t =>
      (t.term_id == term_id && t.school_id == school_id) match {
        case true =>
          TimesheetHelper.purgeTimesheet(t, school_id)
          t.copy(status = Reference.STATUS.DELETE)
        case false => t
      }
    }
    termList.clear()
    termList ++= updatedTermList
    updatedTermList.size match {
      case 0 => false
      case _ => true
    }
  }

}

