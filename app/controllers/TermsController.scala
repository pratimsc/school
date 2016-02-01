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

import models.SchoolHelper
import models.common.TermHelper
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

/**
  * Created by pratimsc on 04/01/16.
  */
class TermsController @Inject()(val messagesApi: MessagesApi, implicit val ws: WSClient) extends Controller with I18nSupport {

  def findByIdAndSchool(term_id: String, school_id: String) = Action.async { implicit request =>
    val sc = SchoolHelper.findById(school_id)
    val tr = TermHelper.findByIdAndSchool(term_id, school_id)
    for {
      school <- sc
      term <- tr
    } yield
      Ok(views.html.terms.TermDetailView(term, school))
  }

  def findAllTimesheetsByTermAndSchool(term_id: String, school_id: String) = Action {
    NotImplemented
  }

  def deleteByIdAndSchool(term_id: String, school_id: String) = Action {
    NotImplemented
  }

  def generateTimesheets(term_id: String, school_id: String) = Action.async {
    val sc = SchoolHelper.findById(school_id)
    val tr = TermHelper.findByIdAndSchool(term_id, school_id)
    val ts = TermHelper.generateTimesheets(term_id, school_id)
    for {
      school <- sc
      term <- tr
      timesheets <- ts
    } yield {
      Ok(views.html.terms.TermTimesheetListView(timesheets, term, school))
    }
  }
}
