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

package services.auth

import java.util.UUID
import javax.inject._

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.auth.{Profile, User}

import scala.concurrent.Future

/**
  * Created by pratimsc on 23/05/16.
  */
class UserService @Inject()(userDao: UserDao) extends IdentityService[User] {
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDao.find(loginInfo)

  def save(user: User) = userDao.save(user)

  def find(id: UUID) = userDao.find(id)

  def confirm(loginInfo: LoginInfo) = userDao.confirm(loginInfo)

  def link(user: User, socialProfile: CommonSocialProfile) = {
    val profile = toProfile(socialProfile)
    if (user.profiles.exists(_.loginInfo == profile.loginInfo)) Future.successful(user) else userDao.link(user, profile)
  }

  def save(socialProfile: CommonSocialProfile) = {
    val profile = toProfile(socialProfile)
    userDao.find(profile.loginInfo).flatMap {
      case None => userDao.save(User(UUID.randomUUID(), List(profile)))
      case Some(user) => userDao.update(profile)
    }
  }

  private def toProfile(p: CommonSocialProfile) = Profile(
    loginInfo = p.loginInfo,
    confirmed = true,
    email = p.email,
    firstName = p.firstName,
    lastName = p.lastName,
    fullName = p.fullName,
    passwordInfo = None,
    oauth1Info = None,
    avatarUrl = p.avatarURL
  )
}
