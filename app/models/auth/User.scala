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

package models.auth

import java.util.UUID

import play.api.libs.json.Json

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info

/**
  * Created by pratimsc on 23/05/16.
  */


case class Profile(
                    loginInfo: LoginInfo,
                    confirmed: Boolean,
                    email: Option[String],
                    firstName: Option[String],
                    lastName: Option[String],
                    fullName: Option[String],
                    passwordInfo: Option[PasswordInfo],
                    oauth1Info: Option[OAuth1Info],
                    avatarUrl: Option[String]) {
}

case class User(id: UUID, profiles: List[Profile]) extends Identity {
  def profileFor(loginInfo: LoginInfo) = profiles.find(_.loginInfo == loginInfo)

  def fullName(loginInfo: LoginInfo) = profileFor(loginInfo).flatMap(_.fullName)
}

object User {
  implicit val passwordInfoJsonFormat = Json.format[PasswordInfo]
  implicit val oauth1InfoJsonFormat = Json.format[OAuth1Info]
  implicit val profileJsonFormat = Json.format[Profile]
  implicit val userJsonFormat = Json.format[User]
}
