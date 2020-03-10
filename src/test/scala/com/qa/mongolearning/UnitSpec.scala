package com.qa.mongolearning
import org.scalatest._
import org.scalatest.concurrent.Futures

abstract class UnitSpec extends FlatSpec with Matchers with
  OptionValues with Inside with Inspectors with Futures