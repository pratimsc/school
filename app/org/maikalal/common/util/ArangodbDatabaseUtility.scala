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

package org.maikalal.common.util

import play.api.libs.json.{JsObject, JsString, Json}
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest}
import play.api.{Logger, Play}

/**
  * Created by pratimsc on 10/01/16.
  */
object ArangodbDatabaseUtility {

  val user = Play.current.configuration.getString("arangodb.user").get
  val password = Play.current.configuration.getString("arangodb.password").get
  val graphUrl = Play.current.configuration.getString("arangodb.url.graph").get
  val documentUrl = Play.current.configuration.getString("arangodb.url.document").get
  val aqlQueryUrl = Play.current.configuration.getString("arangodb.url.aql.query").get
  val cursorUrl = Play.current.configuration.getString("arangodb.url.aql.cursor").get

  def databaseAPIRequest(url: String)(implicit ws: WSClient): WSRequest = {
    ws.url(url)
      .withHeaders("Connection" -> "Close", "Content-Type" -> "application/json")
      .withAuth(user, password, WSAuthScheme.BASIC)
      .withFollowRedirects(true)
  }

  def databaseGraphApiVertexRequest(vertex: String)(implicit ws: WSClient) =
    databaseAPIRequest(s"${graphUrl}/vertex/${vertex}")

  def databaseGraphApiEdgeRequest(edge: String)(implicit ws: WSClient) =
    databaseAPIRequest(s"${graphUrl}/edge/${edge}")

  def databaseGraphApiRequestWithVertexId(id: String)(implicit ws: WSClient) =
    databaseAPIRequest(s"${graphUrl}/vertex/${id}")

  def databaseDocumentApiRequestWithId(id: String)(implicit ws: WSClient) =
    databaseAPIRequest(s"${documentUrl}/${id}")

  def databaseAqlQuerries()(implicit ws: WSClient) = databaseAPIRequest(aqlQueryUrl)

  def databaseCursor()(implicit ws: WSClient) = databaseAPIRequest(cursorUrl)

  def aqlToCursorQueryAsJsonRequetBody(aql: String): JsObject = {
    val aql_json = Json.obj("query" -> JsString(aql))
    Logger.debug(s"AQL Query ->\n [${aql_json}]")
    aql_json
  }

  object DBDocuments {
    val SCHOOLS = "schools"
    val STUDENTS = "students"
    val GUARDIANS = "guardians"
    val FLAT_RATES = "flatRates"
    val BANDED_RATES = "bandedRates"
    val TERMS = "terms"
    val TIMESHEETS_DAILY = "timesheetsDaily"
    val TIMESHEETS_WEEKLY = "timesheetsWeekly"
    val HOLIDAYS = "holidays"
  }

  object DBEdges {
    val SCHOOL_ENROLLED_STUDENT = "enrolled"
    val SCHOOL_DEALS_WITH_GUARDIAN = "deals_with"
    val SCHOOL_HAS_RATE = "has_rate"
    val SCHOOL_HAS_HOLIDAY = "has_holiday"
    val SCHOOL_HAS_TERM = "has_term"
    val STUDENT_RELATED_TO_GUARDIAN = "related_to"
    val TERM_HAS_TIMESHEET = "has_timesheet"
    val STUDENT_HAS_TIMESHEET = "has_timesheet"
  }


}
