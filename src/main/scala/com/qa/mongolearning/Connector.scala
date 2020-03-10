package com.qa.mongolearning

import org.mongodb.scala._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.language.postfixOps

class Connector {
  val mongoClient: MongoClient = makeConnection

  def getCredentials: String = {
    Source.fromResource("userCredentials.txt").getLines().toList.head
  }

  def makeConnection: MongoClient =  {
    MongoClient(getCredentials)
  }


  val database: MongoDatabase = Await.result(Future {
    mongoClient.getDatabase("mydb")
  }, 10 seconds)

}
