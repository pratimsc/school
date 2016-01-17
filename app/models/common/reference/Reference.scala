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

package models.common.reference

import org.joda.time.{DateTime, Hours}

/**
  * Created by pratimsc on 03/01/16.
  */
object Reference {
  val DAILY_TIMESHEET_INITIAL_HOURS = Hours.hours(0)

  object STATUS {
    val ACTIVE = "A"
    val DELETE = "D"
    val INACTIVE = "I"
    val SUSPENDED = "S"
    val BEING_PROCESSED = "P"
    val HISTORICAL = "H"
  }

  object AddressType {
    val OFFICE = "O"
    val RESIDENTIAL = "R"
  }

  object Gender {
    val MALE = "M"
    val FEMALE = "F"
  }

  object GuardianStudentRelationship {
    val WHITE_BRITISH = "WBRI"
    val ASIAN_OR_BRITISH_ASIAN = "AIND"
    val CHINESE = "CHNE"
    val MIXED_WHITE_AND_ASIAN = "MWAS"
  }

  object DatabaseVertex {
    val SCHOOL = "schools"
    val STUDENT = "students"
    val RATE = "rates"
    val TIMESHEET = "timesheets"
  }

  object DatabaseEdges {
    val SCHOOL_ENROLLED_STUDENT = "enrolled"
  }

  val genderList = Seq(Gender.MALE -> "Male", Gender.FEMALE -> "Female")
  val guardianStudentRelationshipList = Seq("GSR01" -> "Father", "GSR02" -> "Mother", "GSR03" -> "Grand Father", "GSR04" -> "Grand Mother", "GSR05" -> "Guardian")
  val ethnicityList = Seq("WBRI" -> "White-British", "AIND" -> "Asian/Asian British-Indian", "CHNE" -> "Chinese", "MWAS" -> "Mixed -White and Asian")
  val specialEducationNeedCodeList = Seq("N" -> "No special educational need", "EYA" -> "Early years / school action", "EYAP" -> "Early years action / school plus", "S" -> "Statement of SEN")

  def businessDate = new DateTime()

}
