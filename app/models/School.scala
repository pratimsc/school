package models

/**
  * Created by pratimsc on 28/12/15.
  */
case class School (school_id:Long, school_name:String, school_address:String)

case class AddSchool(school_name:String, school_address:String)
