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

import javax.inject._

import models._
import models.common._
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by pratimsc on 27/12/15.
  */
class SchoolsController @Inject()(val messagesApi: MessagesApi, implicit val ws: WSClient) extends Controller with I18nSupport {

  def findAll = Action.async { implicit request =>
    SchoolHelper.findAll.map { schools =>
      Ok(views.html.schools.SchoolListView(schools))
    }
  }

  def findById(school_id: String) = Action.async { implicit request =>
    SchoolHelper.findById(school_id).map { school =>
      Ok(views.html.schools.SchoolDetailView(school))
    }
  }

  def findAllStudentsBySchool(school_id: String) = Action.async { implicit request =>
    Logger.debug(s"Getting data for school id [${school_id}] ")
    val sc = SchoolHelper.findById(school_id)
    Logger.debug(s"Getting all the students for the school")
    val st = StudentHelper.findAllBySchool(school_id)
    for {
      school <- sc
      students <- st
    } yield {
      Logger.debug(s"School is [${school.getOrElse("No school was present")}]")
      Ok(views.html.schools.SchoolStudentListView(students, school))
    }
  }

  def findAllRatesBySchool(school_id: String) = Action.async { implicit request =>
    val sc = SchoolHelper.findById(school_id)
    val r = RateHelper.findAllRatesBySchool(school_id)
    for {
      school <- sc
      rates <- r
    } yield
      Ok(views.html.schools.SchoolRatesListView(rates, school))
  }

  def findAllGuardiansBySchool(school_id: String) = Action.async { implicit request =>
    val sc = SchoolHelper.findById(school_id)
    val gu = GuardianHelper.findAllBySchool(school_id)
    for {
      school <- sc
      guardians <- gu
    } yield
      Ok(views.html.schools.SchoolGuardianListView(guardians, school))

  }

  def findAllTermsBySchool(school_id: String) = Action.async { implicit request =>
    SchoolHelper.findById(school_id).map { school =>
      val rates: List[Term] = TermHelper.findAllBySchool(school_id)
      Ok(views.html.schools.SchoolTermsListView(rates, school))
    }
  }

  def findAllWeeklyTimesheetBySchool(school_id: String) = Action.async { implicit request =>
    SchoolHelper.findById(school_id).map { school =>
      val weeklyTimesheets: List[WeeklyTimesheet] = TimesheetHelper.findAllTimesheetsBySchool(school_id)
      Ok(views.html.schools.SchoolTimesheetListView(weeklyTimesheets, school))
    }
  }

  /**
    * Displays a form for registering school
    *
    * @return
    */
  def registerSchool = Action { implicit request =>
    Ok(views.html.schools.AddSchool(SchoolHelper.registerSchoolForm))
  }

  /**
    *
    * @param school_id
    * @return
    */
  def registerStudent(school_id: String) = Action { implicit request =>
    Ok(views.html.schools.AddSchoolStudent(StudentHelper.registerStudentForm, school_id))
  }

  /**
    *
    * @param school_id
    * @return
    */
  def registerTerm(school_id: String) = Action { implicit request =>
    Ok(views.html.schools.AddSchoolTerm(TermHelper.registerTermForm, school_id))
  }

  /**
    * After the form for registering a school is submitted, the school is actually registered.
    *
    * @return
    */
  def addSchool = Action { implicit request =>
    SchoolHelper.registerSchoolForm.bindFromRequest().fold(
      formWithErrors =>
        //Binding failure. Retrieve the form containing error
        BadRequest(views.html.schools.AddSchool(formWithErrors))
      ,
      schoolRegistrationData => {
        //Binding is successful. Wait and get the actual value
        Await.result(SchoolHelper.addSchool(schoolRegistrationData), Duration.Inf) match {
          case Some(school_id) => Redirect(routes.SchoolsController.findById(school_id))
          case None => BadRequest(views.html.schools.AddSchool(SchoolHelper.registerSchoolForm))
        }
      })
  }

  /**
    * After the form for registering a student is submitted, the student is registered with the school.
    *
    * @param school_id
    * @return
    */
  def addStudent(school_id: String) = Action { implicit request =>
    StudentHelper.registerStudentForm.bindFromRequest().fold(
      formsWithError => BadRequest(views.html.schools.AddSchoolStudent(formsWithError, school_id)),
      studentRegistrationData => {
        Await.result(StudentHelper.addStudent(studentRegistrationData, school_id), Duration.Inf) match {
          case Some(student_id) => Redirect(routes.SchoolsController.findAllStudentsBySchool(school_id))
          case _ => BadRequest(views.html.schools.AddSchoolStudent(StudentHelper.registerStudentForm, school_id))
        }
      }
    )
  }

  /**
    * After the form for registering a term is submitted, the student is registered with the school.
    *
    * @param school_id
    * @return
    */
  def addTerm(school_id: String) = Action { implicit request =>
    TermHelper.registerTermForm.bindFromRequest().fold(
      formsWithError => BadRequest(views.html.schools.AddSchoolTerm(formsWithError, school_id)),
      termRegistrationData => {
        val term = models.common.TermHelper.addTerm(termRegistrationData, school_id)
        Redirect(routes.SchoolsController.findAllTermsBySchool(school_id))
      }
    )
  }

  /**
    * After the form for registering a rate is submitted, the rate is registered with the school.
    *
    * @param school_id
    * @return
    */
  def addRate(school_id: String, rateType: Char) = Action { implicit request =>
    rateType match {
      case 'F' =>
        RateHelper.flatRateRegistrationForm.bindFromRequest().fold(
          formsWithError => BadRequest(views.html.schools.AddSchoolRate(formsWithError, RateHelper.bandedRateRegistrationForm, school_id)),
          flatRateRegData => {
            Await.result(RateHelper.addFlatRate(flatRateRegData, school_id), Duration.Inf) match {
              case Some(rate_id) => Redirect(routes.SchoolsController.findAllRatesBySchool(school_id))
              case _ => BadRequest(views.html.schools.AddSchoolRate(RateHelper.flatRateRegistrationForm, RateHelper.bandedRateRegistrationForm, school_id))
            }
          }
        )
      case 'B' =>
        RateHelper.bandedRateRegistrationForm.bindFromRequest().fold(
          formsWithError => BadRequest(views.html.schools.AddSchoolRate(RateHelper.flatRateRegistrationForm, formsWithError, school_id)),
          bandedRateRegData => {
            Await.result(RateHelper.addBandedRate(bandedRateRegData, school_id), Duration.Inf) match {
              case Some(rate_id) => Redirect(routes.SchoolsController.findAllRatesBySchool(school_id))
              case _ => BadRequest(views.html.schools.AddSchoolRate(RateHelper.flatRateRegistrationForm, RateHelper.bandedRateRegistrationForm, school_id))
            }
          }
        )
    }
  }
}
