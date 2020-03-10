package com.qa.mongolearning

import org.bson.conversions.Bson
import org.bson.types._
import org.mongodb.scala._
import org.mongodb.scala.bson.conversions.Bson

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.io.StdIn._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Main extends App {


  def getCredentials: String = {
    Source.fromResource("userCredentials.txt").getLines().toList.head
  }

  def makeConnection: Future[MongoClient] = Future {
    MongoClient(getCredentials)
  }

  //  val mongoClient: MongoClient = Await.result(makeConnection, 10 seconds)
  val mongoClient: MongoClient = MongoClient(getCredentials)


  val database: MongoDatabase = Await.result(Future {
    mongoClient.getDatabase("mydb")
  }, 10 seconds)

  def getCollection(collectionName: String): MongoCollection[Document] = {
    Thread.sleep(1000)
    Await.result(Future {
      database.getCollection(s"$collectionName")
    }, 10 seconds)
  }

  def getPersonDocument(person: Person): Document = {
    Document("_id" -> person.id, "firstName" -> person.firstName, "surname" -> person.surname, "age" -> person.age)
  }

  def getPersonDocumentNoId(person: Person): Document = {
    Document("firstName" -> person.firstName, "surname" -> person.surname, "age" -> person.age)
  }

  def insertPerson(collection: MongoCollection[Document], person: Person): Option[Person] = {
    val docPerson = getPersonDocument(person)
    val observable: Observable[Completed] = collection.insertOne(docPerson)
    var returner: Option[Person] = None

    observable.subscribe(new Observer[Completed] {
      override def onNext(result: Completed): Unit = println("Inserted")

      override def onError(e: Throwable): Unit = {
        println("Failed")
        e.printStackTrace()
      }

      override def onComplete(): Unit = {
        println("Completed")
        returner = Some(person)
      }
    })
    returner
  }

  def getObjectId(entry: Document, key: String): ObjectId = {
    entry.getObjectId(key)
  }

  def getString(entry: Document, key: String): String = {
    entry.getString(key)
  }

  def getInt(entry: Document, key: String): Int = {
    entry.getInteger(key)
  }


  def getPeople(collection: MongoCollection[Document]): Option[List[Person]] = {
    val doc = Await.result(collection.find().toFuture(), 10 seconds)
    var returner: Option[List[Person]] = None
    var listToAdd = new ListBuffer[Person]
    if (doc.nonEmpty) {
      doc.foreach(el => listToAdd += makePersonFromCollectionFind(el))
      returner = Some(listToAdd.toList)
    }
    returner
  }

  def getPerson(collection: MongoCollection[Document], objectId: ObjectId): Option[Person] = {
    val doc = Await.result(collection.find(
      Document("_id" -> objectId)
    ).toFuture(), 10 seconds)
    Some(makePersonFromCollectionFind(doc.head)) orElse None
  }

  def makePersonFromCollectionFind(document: Document): Person = {
    Person(getString(document, "firstName"),
      getString(document, "surname"),
      getInt(document, "age"),
      getObjectId(document, key = "_id")
    )
  }

  def deletePerson(collection: MongoCollection[Document], filter: Bson): Option[Person] = {
    val doc = Await.result(collection.find(
      filter
    ).toFuture(), 10 seconds)
    var returner: Option[Person] = None
    if (doc.nonEmpty) {
      returner = Some(makePersonFromCollectionFind(doc.head))
      Await.result(collection.deleteOne(filter).toFuture(), 10 seconds)
    }
    returner
  }

  def deleteAll(collection: MongoCollection[Document]): Unit = {
    Await.result(collection.deleteMany(Document()).toFuture(), 10 seconds)

  }

  def makeFilterId(objectId: ObjectId): Bson = {
    Document("_id" -> objectId)
  }

  def makeFilterNoId(person: Person): Bson = {
    Document("firstName" -> person.firstName, "surname" -> person.surname, "age" -> person.age)
  }

  def updatePerson(collection: MongoCollection[Document], filter: Bson, updatedPerson: Person): Option[Person] = {
    val updated: Document = getPersonDocumentNoId(updatedPerson)
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


  def createReadUpdateDelete(): Any = readLine() match {
    case "create" =>
      insertPerson(getCollection("person"), getPersonFromInput)

    case "read" =>
      getPeople(getCollection("person"))

    case "update" =>
      println("Old details: ")
      val oldPerson = makeFilterNoId(getPersonFromInput)
      println("New details: ")
      val newPerson = getPersonFromInput
      updatePerson(
        getCollection("person"),
        oldPerson,
        newPerson)
    case "delete" =>
      deletePerson(getCollection("person"), makeFilterNoId(getPersonFromInput))
    case "quit" =>
      -1
    case _ => "Please input a valid choice!"
  }

  def runProgram(): Unit = {
    println("create/read/update/delete/quit?")
    createReadUpdateDelete() match {
      case -1 => mongoClient.close()
      case x =>
        println(x)
        runProgram()
    }

  }

  runProgram()
}
