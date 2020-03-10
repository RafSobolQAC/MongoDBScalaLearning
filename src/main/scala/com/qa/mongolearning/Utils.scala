package com.qa.mongolearning

import scala.io.StdIn.readLine

object Utils {

  def stringInput(): String = {
    readLine()
  }

  @scala.annotation.tailrec
  def intInput(): Int = {
    try {
      readLine().toInt
    } catch {
      case e: NumberFormatException =>
        println("Please input an integer.")
        intInput()

      case _ =>
        println("Unknown error - please try again.")
        intInput()

    }
  }

  def getPersonFromInput: Person = {
    println("Please input the first name. ")
    val firstName = stringInput()
    println("Please input the surname. ")
    val surname = stringInput()
    println("Please input the age. ")
    Person(firstName, surname, intInput())
  }

}
