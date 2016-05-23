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

package dao.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.auth.{Profile, User}

import scala.concurrent.Future

/**
  * Created by pratimsc on 24/05/16.
  */
trait UserDao {
  def find(loginInfo: LoginInfo): Future[Option[User]]

  def find(userId: UUID): Future[Option[User]]

  def save(user: User): Future[User]

  def confirm(loginInfo: LoginInfo): Future[User]

  def link(user: User, profile: Profile): Future[User]

  def update(profile: Profile): Future[User]
}