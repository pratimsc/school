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
import models.common.TimesheetHelper
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

/**
  * Created by pratimsc on 03/01/16.
  */
class TimesheetsController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def findByIdAndSchool(timesheet_id: Long, school_id: Long) = Action { implicit request =>
    val wts = TimesheetHelper.findByIdAndSchool(timesheet_id, school_id)
    val school = SchoolHelper.findById(school_id)
    Ok(views.html.timesheets.DetailWeeklyTimesheets(wts, school))
  }
}
