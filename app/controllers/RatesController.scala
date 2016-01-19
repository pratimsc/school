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
import models.common.RateHelper
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

/**
  * Created by pratimsc on 28/12/15.
  */
class RatesController @Inject()(val messagesApi: MessagesApi, implicit val ws: WSClient) extends Controller with I18nSupport {


  def findRateById(rate_id: String, school_id: String) = Action.async { implicit request =>
    val r = RateHelper.findRateById(rate_id)
    val sc = SchoolHelper.findById(school_id)
    for {
      school <- sc
      rate <- r
    } yield
      Ok(views.html.rates.RateDetailView(rate, school))
  }

  def findAllStudentsByRate(rate_id: String, school_id: String) = Action.async { implicit request =>
    val st = RateHelper.findAllStudentsByRate(rate_id)
    val r = RateHelper.findRateById(rate_id)
    val sc = SchoolHelper.findById(school_id)
    for {
      school <- sc
      rate <- r
      students <- st
    } yield
      Ok(views.html.rates.RateStudentListView(students, rate, school))
  }

  def addRate(school_id: String) = TODO

}
