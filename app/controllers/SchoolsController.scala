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
import models.common.{Rate, RateHelper}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

/**
  * Created by pratimsc on 27/12/15.
  */
class SchoolsController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def findAll = Action { implicit request =>
    Ok(views.html.schools.SchoolListView(SchoolHelper.findAll))
  }

  def findById(school_id: Long) = Action { implicit request =>
    Ok(views.html.schools.SchoolDetailView(SchoolHelper.findById(school_id)))
  }

  def findAllStudentsBySchool(school_id: Long) = Action { implicit request =>
    val students = StudentHelper.findAll(school_id)
    val school = SchoolHelper.findById(school_id)
    //students.foreach(s => println(s"Students are [${s.name}]"))
    Ok(views.html.schools.SchoolStudentListView(students, school))
  }

  def findAllRatesBySchool(school_id: Long) = Action { implicit request =>
    val rates: List[Rate] = RateHelper.findAllRatesBySchool(school_id)
    val school = SchoolHelper.findById(school_id)
    Ok(views.html.schools.SchoolRatesListView(rates, school))
  }

  def findAllGuardiansBySchool(school_id: Long) = Action { implicit request =>
    val guardians = GuardianHelper.findAllBySchool(school_id)
    val school = SchoolHelper.findById(school_id)
    Ok(views.html.schools.SchoolGuardianListView(guardians, school))
  }

  /**
    * Displays a form for registering school
    * @return
    */
  def registerSchool = Action {
    Ok(views.html.schools.AddSchool(SchoolHelper.registerSchoolForm))
  }

  /**
    * After the form for registering a school is submitted, the school is actually registered.
    * @return
    */
  def addSchool = Action { implicit request =>
    SchoolHelper.registerSchoolForm.bindFromRequest().fold(
      formWithErrors =>
        //Binding failure. Retrieve the form containing error
        BadRequest(views.html.schools.AddSchool(formWithErrors))
      ,
      schoolData => {
        //Binding is successful. Got the actual value.
        val school = models.SchoolHelper.addSchool(schoolData)
        Redirect(routes.SchoolsController.findAll())
      }
    )
  }
}
