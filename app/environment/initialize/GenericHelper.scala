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

package environment.initialize

import play.api.libs.ws.{WSAuthScheme, WSClient}

/**
  * Created by pratimsc on 10/01/16.
  */
object GenericHelper {

  val user = "admin"
  val password = "admin"

  def databaseUploadUrl(vertex: String, ws: WSClient) = {
    ws.url(s"http://localhost:2480/document/chelford_preschool_01/${vertex}")
      .withHeaders("Connection" -> "Close", "Content-Type" -> "application/json")
      .withAuth(user, password, WSAuthScheme.BASIC)
      .withFollowRedirects(true)
  }
}
