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

import models._
import models.common.reference.Reference
import models.common.time.Frequency
import models.common.time.Frequency.{frequencyJsonReads, frequencyJsonWrites}
import org.joda.money.Money
import org.joda.time._
import org.maikalal.common.util.{ArangodbDatabaseUtility, DateTimeUtility, MoneyUtility}
import play.api.Logger
import play.api.data.Forms._
import play.api.data.{Forms, Form, Mapping}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.json.{Reads, Writes, _}
import play.api.libs.ws.WSClient

import scala.collection.immutable.List
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by pratimsc on 01/01/16.
  */
case class RateUnit(minor: Hours, description: String)


object RateUnit {
  val perHour = RateUnit(Hours.hours(1), "Per Hour")
  val per8HourDay = RateUnit(Hours.hours(8), "Per Day (8 hours)")
  val per12HourDay = RateUnit(Hours.hours(12), "Per Day (12 hours)")
  val per30HoursWeek = RateUnit(Hours.hours(30), "Per Week (30 hours)")
}

trait Rate {
  def rate_id: String

  def code: String

  def description: String

  def chargeOrRebate: String

  def status: String
}

case class FlatRate(rate_id: String, code: String, description: String, chargeOrRebate: String, status: String, price: Money) extends Rate

case class FlatRateRegistrationData(code: String, description: String, chargeOrRebate: String, status: String, price: Money)

case class BandedRate(rate_id: String, code: String, description: String, chargeOrRebate: String, status: String, bands: List[Band], period: Option[Period]) extends Rate

case class BandedRateRegistrationData(code: String, description: String, chargeOrRebate: String, status: String, bands: List[Band], period: Option[Period])

case class Band(lower_limit: Option[Hours], upper_limit: Option[Hours], rate: Money) {
  require(lower_limit != None || upper_limit != None, "Both lower and upper limits can not be None.")
  require(lower_limit != upper_limit, "Lower limit should be same as the upper limit.")
}

case class RateAppliedToStudent(rate_applied_id: String, rate: Rate, status: String, since: DateTime, until: Option[DateTime], count: Option[Long], frequency: Frequency)


object RateHelper {
  private val flatRateRegistrationFormMapping: Mapping[FlatRateRegistrationData] = mapping(
    "code" -> text(maxLength = Char.MaxValue),
    "description" -> text(maxLength = Char.MaxValue),
    "chargeOrRebate" -> text(minLength = 1, maxLength = 1),
    "status" -> text(minLength = 1, maxLength = 1),
    "price" -> MoneyUtility.moneyFormMapping
  )(FlatRateRegistrationData.apply)(FlatRateRegistrationData.unapply)

  private val bandFormMapping: Mapping[Band] = mapping(
    "lower_limit" -> optional(DateTimeUtility.hourFormMapping),
    "upper_limit" -> optional(DateTimeUtility.hourFormMapping),
    "rate" -> MoneyUtility.moneyFormMapping
  )(Band.apply)(Band.unapply)

  private val bandedRateRegistrationFormMapping: Mapping[BandedRateRegistrationData] = mapping(
    "code" -> text(maxLength = Char.MaxValue),
    "description" -> text(maxLength = Char.MaxValue),
    "chargeOrRebate" -> text(minLength = 1, maxLength = 1),
    "status" -> text(minLength = 1, maxLength = 1),
    "bands" -> Forms.list(bandFormMapping),
    "period" -> optional(DateTimeUtility.periodFormMapping)
  )(BandedRateRegistrationData.apply)(BandedRateRegistrationData.unapply)

  val flatRateRegistrationForm: Form[FlatRateRegistrationData] = Form(flatRateRegistrationFormMapping)
  val bandedRateRegistrationForm: Form[BandedRateRegistrationData] = Form(bandedRateRegistrationFormMapping)

  implicit val hourJsonWrites: Writes[Hours] = (__ \ "hours").write[Int].contramap(_.getHours)
  implicit val hourJsonReads: Reads[Hours] = (__ \ "hours").read[Int].map(Hours.hours _)

  implicit val moneyJsonWrites: Writes[Money] = (__).write[String].contramap(_.toString)
  implicit val moneyJsonReads: Reads[Money] = (__).read[String].map(Money.parse _)

  implicit val periodJsonWrites: Writes[Period] = (__).write[String].contramap(_.toString)
  implicit val periodJsonReads: Reads[Period] = (__).read[String].map(Period.parse _)

  implicit val rateUnitJsonWrites: Writes[RateUnit] = (
    (__ \ "minor").write[Hours] and
      (__ \ "description").write[String]
    ) (unlift(RateUnit.unapply))
  implicit val rateUnitJsonReads: Reads[RateUnit] = (
    (__ \ "minor").read[Hours] and
      (__ \ "description").read[String]
    ) (RateUnit.apply _)

  implicit val flatRateJsonWrites: Writes[FlatRate] = (
    (__ \ "_id").write[String] and
      (__ \ "code").write[String] and
      (__ \ "description").write[String] and
      (__ \ "chargeOrRebate").write[String] and
      (__ \ "status").write[String] and
      (__ \ "price").write[Money]
    ) (unlift(FlatRate.unapply))
  implicit val flatRateJsonReads: Reads[FlatRate] = (
    (__ \ "_id").read[String] and
      (__ \ "code").read[String] and
      (__ \ "description").read[String] and
      (__ \ "chargeOrRebate").read[String] and
      (__ \ "status").read[String] and
      (__ \ "price").read[Money]
    ) (FlatRate.apply _)

  implicit val bandJsonWrites: Writes[Band] = (
    (__ \ "lower_limit").writeNullable[Hours] and
      (__ \ "upper_limit").writeNullable[Hours] and
      (__ \ "rate").write[Money]
    ) (unlift(Band.unapply))
  implicit val bandJsonReads: Reads[Band] = (
    (__ \ "lower_limit").readNullable[Hours] and
      (__ \ "upper_limit").readNullable[Hours] and
      (__ \ "rate").read[Money]
    ) (Band.apply _)

  implicit val bandedRateJsonWrites: Writes[BandedRate] = (
    (__ \ "_id").write[String] and
      (__ \ "code").write[String] and
      (__ \ "description").write[String] and
      (__ \ "chargeOrRebate").write[String] and
      (__ \ "status").write[String] and
      (__ \ "bands").write[List[Band]] and
      (__ \ "period").writeNullable[Period]
    ) (unlift(BandedRate.unapply))
  implicit val bandedRateJsonReads: Reads[BandedRate] = (
    (__ \ "_id").read[String] and
      (__ \ "code").read[String] and
      (__ \ "description").read[String] and
      (__ \ "chargeOrRebate").read[String] and
      (__ \ "status").read[String] and
      (__ \ "bands").read[List[Band]] and
      (__ \ "period").readNullable[Period]
    ) (BandedRate.apply _)

  //case class RateAppliedToStudent(rate_applied_id: String, rate: Rate, status: String, since: DateTime, until: Option[DateTime], count: Option[Long], frequency: Frequency)
  /*implicit val rateAppliedToStudentJsonWrites: Writes[RateAppliedToStudent] = (
    (__ \ "_id").write[String] and
      (__ \ "rate").write[Rate] and
      (__ \ "status").write[String] and
      (__ \ "since").write[DateTime] and
      (__ \ "until").writeNullable[DateTime] and
      (__ \ "count").writeNullable[Long] and
      (__ \ "frequency").write[Frequency]
    ) (unlift(RateAppliedToStudent.unapply))
*/
  implicit val rateAppliedToStudentJsonWrites: Writes[RateAppliedToStudent] = new Writes[RateAppliedToStudent] {
    override def writes(o: RateAppliedToStudent): JsValue = Json.obj(
      "_id" -> o.rate_applied_id,
      "rate_type" -> (o.rate match {
        case f: FlatRate => "F"
        case b: BandedRate => "B"
      }),
      "rate" -> (o.rate match {
        case f: FlatRate => f
        case b: BandedRate => b
      }),
      "status" -> o.status,
      "since" -> o.since,
      "until" -> o.until,
      "count" -> o.count,
      "frequency" -> o.frequency
    )
  }

  implicit val rateAppliedToStudentJsonReads: Reads[RateAppliedToStudent] = new Reads[RateAppliedToStudent] {
    override def reads(json: JsValue): JsResult[RateAppliedToStudent] = {
      try {
        JsSuccess(RateAppliedToStudent(
          (json \ "_id").as[String],
          (json \ "rate_type").as[String] match {
            case "F" => (json \ "rate").as[FlatRate]
            case "B" => (json \ "rate").as[BandedRate]
          },
          (json \ "status").as[String],
          (json \ "since").as[DateTime],
          (json \ "until").asOpt[DateTime],
          (json \ "count").asOpt[Long],
          (json \ "frequency").as[Frequency]
        ))
      } catch {
        case e: Throwable => JsError(e.getMessage)
      }
    }
  }

  /* implicit val rateAppliedToStudentJsonReads1: Reads[RateAppliedToStudent] = (
     (__ \ "_id").read[String] and
       ((__ \ "rate_type").read[String] match {
         case "F" => (__ \ "rate").read[FlatRate]
         case "B" => (__ \ "rate").read[BandedRate]
       }) and
       (__ \ "status").read[String] and
       (__ \ "since").read[DateTime] and
       (__ \ "until").read[DateTime] and
       (__ \ "count").read[Long] and
       (__ \ "frequency").read[Frequency]
     ) (Frequency.parse _)

 */
  def findRateById(rate_id: String)(implicit ws: WSClient): Future[Option[Rate]] = ???

  def findAllRatesBySchool(school_id: String)(implicit ws: WSClient): Future[List[Rate]] = {
    val aql =
      s"""
         |FOR sc in schools
         |filter sc._id == "${school_id}" && sc.status != "${Reference.STATUS.DELETE}"
         |FOR e in has_rate
         |filter e._from == sc._id
         |FOR fr in flatRates
         |filter fr._id == e._to && fr.status != "${Reference.STATUS.DELETE}"
         |return fr
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"List of the rates based on the school [${school_id}] is -> ${res.json}")
      val rates: List[FlatRate] = (res.json \ "result").as[List[FlatRate]]
      rates
    }
  }

  def findAllRatesByStudent(student_id: String)(implicit ws: WSClient): Future[List[Rate]] = ???

  def findAllAppliedRatesByStudent(student_id: String)(implicit ws: WSClient): Future[List[RateAppliedToStudent]] = ???

  def findAllSchoolsByRate(rate_id: String)(implicit ws: WSClient): Future[List[School]] = ???

  def findAllStudentsByRate(rate_id: String)(implicit ws: WSClient): Future[List[Student]] = ???

  def addFlatRate(fr: FlatRateRegistrationData, school_id: String)(implicit ws: WSClient): Future[Some[String]] = {
    val fr_json: JsValue = Json.toJson(fr)
    val aql =
      s"""
         |FOR sc in schools
         |  FILTER sc._id == "${school_id}" && sc.status != "${Reference.STATUS.DELETE}"
         |  INSERT ${fr_json} in flatRates
         |  let fr = NEW
         |  INSERT {"_from":sc._id, "_to":fr._id}in has_rate
         |  let rel = NEW
         |return {"flatRate":fr, "school":sc, "relation":rel}
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the flat rate added ->${Json.prettyPrint(res.json)}")
      val result = (res.json \ "result") (0)
      val sc_json = result \ "school"
      val rate_json = result \ "flatRate"
      val rate_id = (rate_json \ "_id").as[String]
      Some(rate_id)
    }
  }

  def addBandedRate(br: BandedRateRegistrationData, school_id: String)(implicit ws: WSClient): Future[Some[String]] = {
    val br_json: JsValue = Json.toJson(br)
    val aql =
      s"""
         |FOR sc in schools
         |  FILTER sc._id == "${school_id}" && sc.status != "${Reference.STATUS.DELETE}"
         |  INSERT ${br_json} in bandedRates
         |  let br = NEW
         |  INSERT {"_from":sc._id, "_to":br._id}in has_rate
         |  let rel = NEW
         |return {"bandedRate":br, "school":sc, "relation":rel}
      """.stripMargin
    ArangodbDatabaseUtility.databaseCursor().post(ArangodbDatabaseUtility.aqlToCursorQueryAsJsonRequetBody(aql)).map { res =>
      Logger.debug(s"Details of the banded rate added ->${Json.prettyPrint(res.json)}")
      val result = (res.json \ "result") (0)
      val sc_json = result \ "school"
      val rate_json = result \ "bandedRate"
      val rate_id = (rate_json \ "_id").as[String]
      Some(rate_id)
    }
  }
}