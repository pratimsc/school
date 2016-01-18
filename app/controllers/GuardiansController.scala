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
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

/**
  * Created by pratimsc on 31/12/15.
  */
class GuardiansController @Inject()(val messagesApi: MessagesApi, implicit val ws: WSClient) extends Controller with I18nSupport {

  def findByIdAndSchool(guardian_id: String, school_id: String) = Action.async { implicit request =>
    GuardianHelper.findByIdAndSchool(guardian_id, school_id).map { guardian =>
      Ok(views.html.guardians.GuardianDetailView(guardian))
    }
  }

  def findById(guardian_id: String) = Action.async { implicit request =>
    GuardianHelper.findById(guardian_id).map { guardian =>
      Ok(views.html.guardians.GuardianDetailView(guardian))
    }
  }

  def findAllStudentsByGuardian(guardian_id: String) = Action.async { implicit request =>
    val st = StudentHelper.findAllStudentsByGuardianId(guardian_id)
    val gu = GuardianHelper.findById(guardian_id)
    for {
      students <- st
      guardian <- gu
    } yield
      Ok(views.html.guardians.GuardianStudentListView(students, guardian))
  }

  def findAllSchoolByGuardian(guardian_id: String) = Action.async { implicit request =>
    val sc = SchoolHelper.findAllSchoolsByGuardianId(guardian_id)
    val gu = GuardianHelper.findById(guardian_id)
    for {
      schools <- sc
      guardian <- gu
    } yield
      Ok(views.html.guardians.GuardianSchoolListView(schools, guardian))
  }

  def updateGuardian(guardian_id: String) = Action { implicit request =>
    Ok("")
  }
}
