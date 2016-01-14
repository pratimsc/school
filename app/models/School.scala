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

package models

import com.tinkerpop.blueprints.impls.orient.OrientVertex
import models.common.reference.Reference
import models.common.{Address, AddressHelper, AddressRegistrationData}
import org.maikalal.common.util.{DatabaseUtility, DateUtility}
import play.api.data.Forms._
import play.api.data._

import scala.collection.JavaConversions._

/**
  * Created by pratimsc on 28/12/15.
  */

case class School(school_id: String, school_name: String, school_address: Address, status: String)

case class SchoolRegistration(school_name: String, school_address: AddressRegistrationData)

object SchoolHelper {

  val schoolFormMapping = mapping(
    "school_name" -> nonEmptyText(minLength = 10, maxLength = Char.MaxValue),
    "school_address" -> AddressHelper.addressFormMapping
  )(SchoolRegistration.apply)(SchoolRegistration.unapply)

  val registerSchoolForm = Form(schoolFormMapping)

  /*
   * A non persistence storage for Schools
   */
  val schoolList = scala.collection.mutable.MutableList[School](
    School("SC1", "Mongo Bongo School", AddressHelper.findById(1).get, "A"),
    School("SC2", "Tooko Takka School", AddressHelper.findById(2).get, "A")
  )

  def findAll: List[School] = schoolList.toList

  //def findById(school: Long): Option[School] = schoolList.find(_.school_id == school)
  def findById(school: String): Option[School] = DatabaseUtility.executeWithNoTransaction(DatabaseUtility.graphFactory.getNoTx)(g => {
    g.getVertices("School.school_id", school).iterator().toList match {
      case h :: t => ???
      case Nil => ???
    }
  })

  def findAllSchoolsByGuardianId(guardian_id: Long) = GuardianHelper.findById(guardian_id) match {
    case Some(guardian) =>
      guardian.students.groupBy(_.student.school).map(_._1).toList
    case None => Nil
  }

  def addSchool(s: SchoolRegistration): Option[School] = DatabaseUtility.getUniqueSchoolId match {
    case Some(school_id) =>
      AddressHelper.manufactureAddress(s.school_address) match {
        case Some(address) =>
          val school = School("SC" + school_id, s.school_name, address, Reference.STATUS.ACTIVE)

          DatabaseUtility.executeWithTransaction(DatabaseUtility.graphFactory.getTx)(g => {
            val scV: OrientVertex = g.addVertex("School", null)
            scV.setProperties(mapAsJavaMap(Map("school_id" -> school.school_id, "school_name" -> school.school_name, "status" -> Reference.STATUS.ACTIVE)))
            val addV = g.addVertex("Address", null)
            addV.setProperties(mapAsJavaMap(DatabaseUtility.caseClassToPropertyMap(address)))
            scV.addEdge("has_address", addV, null, null, mapAsJavaMap(Map("type" -> Reference.AddressType.OFFICE, "status" -> Reference.STATUS.ACTIVE, "since" -> DateUtility.fommattedDate(Reference.businessDate))))
            scV
          }) match {
            case Some(_) =>
              //School and Address successfully added
              schoolList += school
              Some(school)
            case None => None //No data was added
          }
        case None => None //Could not get the Address
      }
    case None => None //Could not get School Id
  }

}

