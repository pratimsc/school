package models

import models.common.{Address, AddressHelper, Name}

/**
  * Created by pratimsc on 31/12/15.
  */
case class Parent(parent_id: Long, name: Name, address: Address, gender:String,email: Option[String], national_insurance_number: Option[String], students: List[Student])

case class ()

object ParentHelper {

  /*
   * A non persistence storage for Schools
   */
  val parentList = scala.collection.mutable.MutableList[Parent](
    Parent(1, Name("Chocko", Some("Slaughter Man"), "Funny"), AddressHelper.findById(3).get, "M",Some("chocko.funny@blahblah.com"), Some("NI12345781"), List(StudentHelper.findById(1, 1).get, StudentHelper.findById(3, 2).get)),
    Parent(2, Name("Lisa", Some("Pussy Cat"), "Angry"), AddressHelper.findById(3).get, "F", Some("lisa.angry@blahblah.com"), Some("NI12345782"), List(StudentHelper.findById(3, 2).get)),
    Parent(3, Name("Chocko", Some("Slaughter Man"), "Funny"), AddressHelper.findById(3).get, "F", Some("chocko.funny.blahblah.com"), Some("NI1234578"), List(StudentHelper.findById(2, 1).get))
  )

  /*
    Parent 1 = Student(1, SchoolHelper.findById(1).get, Name("George", Some("Washington"), "DC"), "A", "M", AddressHelper.findById(3).get, DateTime.parse("2012-1-1"), Some("george.dc.washington@blahblah.com"), Some("White American"), Some("Sen code 01")),
    Student(2, SchoolHelper.findById(1).get, Name("Neon", Some("Anderson"), "Matrix"), "A", "M", AddressHelper.findById(4).get, DateTime.parse("2012-1-2"), Some("leon.matrix.anderson@blahblah.com"), Some("White American"), Some("Sen code 01")),
    Parent 1,Parent 2 = Student(3, SchoolHelper.findById(2).get, Name("Lisa", Some("Butcher"), "Gamer"), "A", "F", AddressHelper.findById(5).get, DateTime.parse("2012-1-3"), Some("lisa.gamer.butcher@blahblah.com"), Some("Black American"), Some("Sen code 01")),
    Student(4, SchoolHelper.findById(2).get, Name("Bhima", Some("Pandu"), "Mahabharata"), "A", "M", AddressHelper.findById(6).get, DateTime.parse("2012-1-4"), Some("bhima.mahabharata.pandu@blahblah.com"), Some("Ancient Indian"), Some("Sen code 01")),
    Student(5, SchoolHelper.findById(2).get, Name("Thor", Some("Hammer"), "Pagan God"), "A", "M", AddressHelper.findById(7).get, DateTime.parse("2012-1-5"), Some("thor.pagan.god.hammer@blahblah.com"), Some("Ancient God"), Some("Sen code 01"))
  )*/

  def findAll(school_id: Long): List[Student] = ???

  def findById(student_id: Long, school_id: Long): Option[Student] = ???

  def addParent(s: StudentRegistrationData): Option[Student] = ???

}