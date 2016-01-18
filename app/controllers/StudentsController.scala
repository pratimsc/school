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
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

/**
  * Created by pratimsc on 27/12/15.
  */
class StudentsController @Inject()(val messagesApi: MessagesApi, implicit val ws: WSClient) extends Controller with I18nSupport {

  def findByIdAndSchool(student_id: String, school_id: String) = Action.async { implicit request =>
    val sc = SchoolHelper.findById(school_id)
    val st = StudentHelper.findByIdAndSchool(student_id, school_id)
    for {
      school <- sc
      student <- st
    } yield
      Ok(views.html.students.StudentDetailView(student, school))
  }


  def findAllGuardiansByStudent(student_id: String, school_id: String) = Action.async {
    implicit request =>
      val gu: Future[List[Guardian]] = GuardianHelper.findAllByStudent(student_id)
      val sc: Future[Option[School]] = SchoolHelper.findById(school_id)
      val st: Future[Option[Student]] = StudentHelper.findByIdAndSchool(student_id, school_id)
      for {
        school <- sc
        student <- st
        guardians <- gu
      } yield {
        school.map(sc => println(s"School after adding guardian is ${sc}"))
        student.map(st => println(s"Student after adding guardian is ${st}"))
        Ok(views.html.students.StudentGuardianListView(guardians, student, school))
      }
  }

  def findAllAppliedRates(student_id: String, school_id: String) = Action.async {
    implicit request =>
      val rates = RateHelper.findAllAppliedRatesByStudent(student_id)
      val sc = SchoolHelper.findById(school_id)
      val st = StudentHelper.findByIdAndSchool(student_id, school_id)
      for {
        school <- sc
        student <- st
      } yield
        Ok(views.html.students.StudentRateListView(rates, student, school))
  }

  def findAllTimesheetsByStudentAndSchool(student_id: String, school_id: String) = Action.async {
    implicit request =>
      val weeklyTimesheets = TimesheetHelper.findAllTimesheetsByStudent(student_id)
      val sc = SchoolHelper.findById(school_id)
      val st = StudentHelper.findByIdAndSchool(student_id, school_id)
      for {
        school <- sc
        student <- st
      } yield
        Ok(views.html.students.StudentTimesheetListView(weeklyTimesheets, student, school))
  }

  def registerGuardian(student_id: String, school_id: String) = Action {
    implicit request =>
      Ok(views.html.students.AddStudentGuardian(GuardianHelper.registerGuardianForm, student_id, school_id))
  }

  def addGuardian(student_id: String, school_id: String) = Action {
    implicit request =>
      GuardianHelper.registerGuardianForm.bindFromRequest().fold(
        formWithErrors =>
          //Binding failure. Retrieve the form containing error
          BadRequest(views.html.students.AddStudentGuardian(formWithErrors, student_id, school_id))
        ,
        guardianRegistratonData => {
          //Binding got successful
          Await.result(GuardianHelper.addGuardian(guardianRegistratonData, student_id, school_id), Duration.Inf) match {
            case Some(guardian_id) => Redirect(routes.StudentsController.findAllGuardiansByStudent(student_id, school_id))
            case None => BadRequest(views.html.students.AddStudentGuardian(GuardianHelper.registerGuardianForm, student_id, school_id))
          }
        }
      )
  }


  def addRate(student_id: String, school_id: String) = TODO

  def addRebate(student_id: String, school_id: String) = TODO

}
