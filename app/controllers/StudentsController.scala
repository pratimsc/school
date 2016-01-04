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

import models.common.{RateHelper, TimesheetHelper}
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

  def findAllTimesheetsByStudentAndSchool(student_id: Long, school_id: Long) = Action { implicit request =>
    val weeklyTimesheets = TimesheetHelper.findAllTimesheetsByStudent(student_id)
    val student = StudentHelper.findById(student_id, school_id)
    Ok(views.html.students.StudentTimesheetListView(weeklyTimesheets, student))
  }

  def registerGuardian(student_id: Long, school_id: Long) = Action { implicit request =>
    Ok(views.html.students.AddStudentGuardian(GuardianHelper.registerGuardianForm, student_id, school_id))
  }

  def addGuardian(student_id: Long, school_id: Long) = Action { implicit request =>
    GuardianHelper.registerGuardianForm.bindFromRequest().fold(
      formWithErrors =>
        //Binding failure. Retrieve the form containing error
        BadRequest(views.html.students.AddStudentGuardian(formWithErrors, student_id, school_id))
      ,
      guardianRegistratonData => {
        //Binding got successful
        val guardian = GuardianHelper.addGuardian(guardianRegistratonData, student_id, school_id)
        Redirect(routes.StudentsController.findAllGuardiansByStudent(student_id, school_id))
      }
    )
  }


  def addRate(student: Long, school: Long) = TODO

  def addRebate(student: Long, school: Long) = TODO

}
