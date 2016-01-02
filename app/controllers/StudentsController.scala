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

package controllers

import javax.inject.Inject

import models.common.RateHelper
import models.{GuardianHelper, StudentHelper}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

/**
  * Created by pratimsc on 27/12/15.
  */
class StudentsController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {


  def findByIdAndSchool(student_id: Long, school_id: Long) = Action { implicit request =>
    val student = StudentHelper.findById(student_id, school_id)
    //student.map(s => println(s"[${s.student_id}]"))
    Ok(views.html.students.StudentDetailView(student))
  }

  def findAllGuardiansByStudent(student_id: Long, school_id: Long) = Action { implicit request =>
    val guardians = GuardianHelper.findAllByStudent(student_id)
    val student = StudentHelper.findById(student_id, school_id)
    Ok(views.html.students.StudentGuardianListView(guardians, student))
  }

  def findAllAppliedRates(student_id: Long, school_id: Long) = Action { implicit request =>
    val rates = RateHelper.findAllAppliedRatesByStudent(student_id)
    val student = StudentHelper.findById(student_id, school_id)
    Ok(views.html.students.StudentRateListView(rates, student))
  }


  def addStudent(school: Long) = TODO

  def addRate(student: Long, school: Long) = TODO

  def addRebate(student: Long, school: Long) = TODO

}
