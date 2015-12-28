package controllers

import play.api.mvc.Controller

/**
  * Created by pratimsc on 27/12/15.
  */
object StudentsController extends Controller {

  def findById(student: Long, school: Long) = {
    TODO
  }

  def appliedRates(student: Long, school: Long) = TODO

  def appliedRebates(student: Long, school: Long) = TODO

  def addStudent(school: Long) = TODO

  def addRate(student: Long, school: Long) = TODO

  def addRebate(student: Long, school: Long) = TODO

}
