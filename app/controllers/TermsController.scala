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
import models.common.{TermHelper, TimesheetHelper, WeeklyTimesheet}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

/**
  * Created by pratimsc on 04/01/16.
  */
class TermsController @Inject()(val messagesApi: MessagesApi, implicit val ws: WSClient) extends Controller with I18nSupport {

  def findByIdAndSchool(term_id: Long, school_id: String) = Action.async { implicit request =>
    SchoolHelper.findById(school_id).map { school =>
      val term = TermHelper.findById(term_id, school_id)
      Ok(views.html.terms.TermDetailView(term, school))
    }
  }

  def findAllTimesheetsByTermAndSchool(term_id: Long, school_id: String) = Action.async { implicit request =>
    SchoolHelper.findById(school_id).map { school =>
      val weeklyTimesheets: List[WeeklyTimesheet] = TimesheetHelper.findAllTimesheetsByTermAndSchool(term_id, school_id)

      val term = TermHelper.findById(term_id, school_id)
      Ok(views.html.terms.TermTimesheetListView(weeklyTimesheets, term, school))
    }
  }

  def deleteByIdAndSchool(term_id: Long, school_id: String) = Action.async { implicit request =>
    SchoolHelper.findById(school_id).map { school =>
      val term = TermHelper.purgeById(term_id, school_id)
      Redirect(routes.SchoolsController.findAllTermsBySchool(school_id))
    }
  }
}
