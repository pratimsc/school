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

import models.{GuardianHelper, SchoolHelper, StudentHelper}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

/**
  * Created by pratimsc on 31/12/15.
  */
class GuardiansController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def findByIdAndSchool(guardian_id: Long, school_id: Long) = Action { implicit request =>
    val guardian = GuardianHelper.findById(guardian_id, school_id)
    Ok(views.html.guardians.GuardianDetailView(guardian))
  }

  def findById(guardian_id: Long) = Action { implicit request =>
    val guardian = GuardianHelper.findById(guardian_id)
    Ok(views.html.guardians.GuardianDetailView(guardian))
  }

  def findAllStudentsByGuardian(guardian_id: Long) = Action { implicit request =>
    val students = StudentHelper.findAllStudentsByGuardianId(guardian_id)
    val guardian = GuardianHelper.findById(guardian_id)
    Ok(views.html.guardians.GuardianStudentListView(students, guardian))
  }

  def findAllSchoolByGuardian(guardian_id: Long) = Action { implicit request =>
    val schools = SchoolHelper.findAllSchoolsByGuardianId(guardian_id)
    val guardian = GuardianHelper.findById(guardian_id)
    Ok(views.html.guardians.GuardianSchoolListView(schools, guardian))
  }
}
