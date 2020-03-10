package com.qa.mongolearning

import org.bson.conversions.Bson
import org.bson.types._
import org.mongodb.scala._
import org.mongodb.scala.bson.conversions.Bson

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.StdIn._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import com.qa.mongolearning.Utils._

class PersonServices(testMode: Boolean) {
  def this() {
    this(false)
  }
  var connector: Connector = _
  if (!testMode) {
    connector = getConnector
  }

  def getConnector: Connector = {
    new Connector
  }

  val outputParser = new OutputParser(connector)


  def insertPerson(collection: MongoCollection[Document], person: Person): Unit = {
    val docPerson = outputParser.getPersonDocument(person)
    collection.insertOne(docPerson).toFuture().onComplete {
      case Success(_) => giveOutput(Some(person))
      case Failure(error) => error.printStackTrace()
        giveOutput(None)
    }
  }


  def getPeople(collection: MongoCollection[Document]): Option[List[Person]] = {
    val doc = Await.result(collection.find().toFuture(), 10 seconds)
    var returner: Option[List[Person]] = None
    var listToAdd = new ListBuffer[Person]
    if (doc.nonEmpty) {
      doc.foreach(el => listToAdd += outputParser.makePersonFromCollectionFind(el))
      returner = Some(listToAdd.toList)
    }
    returner
  }

  def getPerson(collection: MongoCollection[Document], objectId: ObjectId): Option[Person] = {
    val doc = Await.result(collection.find(
      Document("_id" -> objectId)
    ).toFuture(), 10 seconds)
    Some(outputParser.makePersonFromCollectionFind(doc.head)) orElse None
  }


  def deletePerson(collection: MongoCollection[Document], filter: Bson): Option[Person] = {
    val doc = Await.result(collection.find(
      filter
    ).toFuture(), 10 seconds)
    var returner: Option[Person] = None
    if (doc.nonEmpty) {
      returner = Some(outputParser.makePersonFromCollectionFind(doc.head))
      Await.result(collection.deleteOne(filter).toFuture(), 10 seconds)
    }
    returner
  }

  def deleteAll(collection: MongoCollection[Document]): Unit = {
    Await.result(collection.deleteMany(Document()).toFuture(), 10 seconds)

  }


  def updatePerson(collection: MongoCollection[Document], filter: Bson, updatedPerson: Person): Option[Person] = {
    val updated: Document = outputParser.getPersonDocumentNoId(updatedPerson)
    var returner: Option[Person] = None
    collection.replaceOne(
      filter,
      updated
    ).toFuture().onComplete {
      case Success(value) =>
        println("Successfully updated.")
        println(value)
        returner = Some(updatedPerson)
      case Failure(error) =>
        println("Failed to update user!")
        error.printStackTrace()

    }
    returner

  }



  def createReadUpdateDelete(): Any = readLine() match {
    case "create" =>
      insertPerson(outputParser.getCollection("person"), getPersonFromInput)

    case "read" =>
      getPeople(outputParser.getCollection("person"))

    case "update" =>
      println("Old details: ")
      val oldPerson = outputParser.makeFilterNoId(getPersonFromInput)
      println("New details: ")
      val newPerson = getPersonFromInput
      updatePerson(
        outputParser.getCollection("person"),
        oldPerson,
        newPerson)
    case "delete" =>
      deletePerson(outputParser.getCollection("person"), outputParser.makeFilterNoId(getPersonFromInput))
    case "quit" =>
      -1
    case _ => "Please input a valid choice!"
  }

  def runProgram(): Unit = {
    println("create/read/update/delete/quit?")
    createReadUpdateDelete() match {
      case -1 => connector.mongoClient.close()
      case x =>
        println(x)
        runProgram()
    }

  }

}
