package com.qa.mongolearning

import org.mongodb.scala._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.language.postfixOps

class Connector {
  val mongoClient: MongoClient = makeConnection
  val database: MongoDatabase = getMongoDB

  def getCredentials: String = {
    println("I'm getting credentials!")
    Source.fromResource("userCredentials.txt").getLines().toList.head
  }
  def makeConnection: MongoClient =  {
    MongoClient(getCredentials)
  }

  def getMongoDB: MongoDatabase = Await.result(Future {
    mongoClient.getDatabase("mydb")
  }, 10 seconds)

}
