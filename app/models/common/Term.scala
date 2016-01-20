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
import play.api.libs.ws.WSClient
import play.api.libs.ws.ning.NingWSClient

import scala.collection.mutable
import scala.collection.mutable.MutableList
import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
  * Created by pratimsc on 04/01/16.
  */
case class Term(term_id: Long, school_id: String, begin: DateTime, finish: DateTime, status: String)

case class TermRegistrationData(begin: DateTime, finish: DateTime)

object TermHelper {

  private val initialDate = DateTime.parse("20160104", ISODateTimeFormat.basicDate)
  var term_unique_id_count = 6
  val termList: mutable.MutableList[Term] = mutable.MutableList()
  /** MutableList(
      * Term(1, SchoolHelper.schoolList.get(0).get.school_id, initialDate, initialDate.plusWeeks(12).plusDays(6), "A"),
      * Term(2, SchoolHelper.schoolList.get(0).get.school_id, initialDate.plusWeeks(13), initialDate.plusWeeks(13 + 12).plusDays(6), "A"),
      * Term(3, SchoolHelper.schoolList.get(0).get.school_id, initialDate.plusWeeks(13 + 13), initialDate.plusWeeks(13 + 13 + 12).plusDays(6), "A"),
      * Term(4, SchoolHelper.schoolList.get(1).get.school_id, initialDate, initialDate.plusWeeks(12).plusDays(6), "A"),
      * Term(5, SchoolHelper.schoolList.get(1).get.school_id, initialDate.plusWeeks(13), initialDate.plusWeeks(13 + 12).plusDays(6), "A"),
      * Term(6, SchoolHelper.schoolList.get(1).get.school_id, initialDate.plusWeeks(13 + 13), initialDate.plusWeeks(13 + 13 + 12).plusDays(6), "A")
    * )
    **/
  //nitialize all termsheets
  implicit val ws = NingWSClient()

  private val termFormMaping = mapping(
    "begin_date" -> jodaDate("yyyy-MM-dd"),
    "finish_date" -> jodaDate("yyyy-MM-dd")
  )(TermRegistrationData.apply)(TermRegistrationData.unapply)

  val registerTermForm = Form(termFormMaping)


  def findAllBySchool(school_id: String): List[Term] = ???

  def findById(term_id: Long, school_id: String): Option[Term] = ???

  def addTerm(trd: TermRegistrationData, school_id: String)(implicit ws: WSClient): Option[Term] = ???


  def purgeById(term_id: Long, school_id: String): Boolean = ???

}

