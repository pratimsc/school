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

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.Play

/**
  * Created by pratimsc on 31/12/15.
  */
object DateUtility {

  val key_date_format: String = Play.current.configuration.getString("application.date.display.format").getOrElse("YYYY-MM-dd")

  def fommattedDate(date: DateTime, format: String = key_date_format) = DateTimeFormat.forPattern(format).print(date)
}
