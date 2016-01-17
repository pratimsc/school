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

package models.common

import play.api.data.Forms._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json.{Writes, _}

/**
  * Created by pratimsc on 30/12/15.
  */
case class Name(first: String, middle: Option[String], last: String)

object NameHelper {

  implicit val nameJsonWrites: Writes[Name] = (
    (__ \ "first").write[String] and
      (__ \ "middle").writeNullable[String] and
      (__ \ "last").write[String]
    ) (unlift(Name.unapply))

  implicit val nameJsonReads: Reads[Name] = (
    (__ \ "first").read[String] and
      (__ \ "middle").readNullable[String] and
      (__ \ "last").read[String]
    ) (Name.apply _)

  val nameMapping = mapping(
    "first" -> nonEmptyText(maxLength = Char.MaxValue),
    "middle" -> optional(text(maxLength = Char.MaxValue)),
    "last" -> nonEmptyText(maxLength = Char.MaxValue)
  )(Name.apply)(Name.unapply)

}
