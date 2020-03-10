package com.qa.mongolearning

import org.bson.types._
import org.mongodb.scala._
import org.mongodb.scala.bson.{BsonDocument, BsonString}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Updates._
import scala.io.StdIn._
import scala.util.{Failure, Success}
object Main extends App {


  def getCredentials: String = {
    Source.fromResource("userCredentials.txt").getLines().toList.head
  }

  val mongoClient: MongoClient = MongoClient(getCredentials)

  val database: MongoDatabase = mongoClient.getDatabase("mydb")
  val collection: MongoCollection[Document] = database.getCollection("person")

//  def getNextSimpleId(name: String) = {
//    val doc = Await.result(counter.find().head(), 10 seconds)
//    val previousId = doc.get("seq").get.asInt32().getValue
//    val filter: Bson = Document("_id" -> name)
//    val updated: Document = Document("seq" -> {previousId+1})
//
//    counter.replaceOne(
//      filter,
//      updated
//    )
//      .toFuture()
//      .onComplete {
//      case Success(value) => println(s"Updated! Now $value")
//      case Failure(error) => error.printStackTrace()
//    }
//
//    previousId
//
//  }


  def getCollection(collectionName: String): MongoCollection[Document] = {
    database.getCollection(s"$collectionName")
  }

  def getPersonDocument(person: Person): Document = {
    Document("_id" -> person.id, "firstName" -> person.firstName, "surname" -> person.surname, "age" -> person.age)
  }

  def getPersonDocumentNoId(person: Person): Document = {
    Document("firstName" -> person.firstName, "surname" -> person.surname, "age" -> person.age)
  }

  def insertPerson(collection: MongoCollection[Document], person: Person): Option[Person]= {
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
//    val doc = Await.result(collection.find().first().head(), 10 seconds)
    val doc = Await.result(collection.find().toFuture(), 10 seconds)
    var returner: Option[List[Person]] = None
    var listToAdd = new ListBuffer[Person]
    if (doc.nonEmpty) {
      doc.foreach(el => listToAdd += makePersonFromCollectionFind(el))
      returner = Some(listToAdd.toList)
    }
    returner
  }
  def makePersonFromCollectionFind(document: Document): Person = {
    Person(getString(document, "firstName"),
      getString(document, "surname"),
      getInt(document, "age"),
      getObjectId(document, key="_id")
    )
  }
  def deletePerson(collection: MongoCollection[Document], personId: ObjectId): Option[Person] = {
    val doc = Await.result(collection.find(
      Document("_id" -> personId)
    ).toFuture(), 10 seconds)
    var returner: Option[Person] = None
    if (doc.nonEmpty) {
      returner = Some(makePersonFromCollectionFind(doc.head))
      Await.result(collection.deleteOne(Document("_id" -> personId)).toFuture(), 10 seconds)
    }
    returner
  }

  def deleteAll(collection: MongoCollection[Document]): Unit = {
    Await.result(collection.deleteMany(Document()).toFuture(), 10 seconds)

  }

  def updatePerson(collection: MongoCollection[Document], personId: ObjectId, updatedPerson: Person): Option[Person] = {
    val filter: Bson = Document("_id" -> personId)
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



    //  println(insertPerson(getCollection("person"),Person("Bobby","Tables",12)))
    //  println(insertPerson(getCollection("person"),Person("Billy","Tables",13)))
    //  println(insertPerson(getCollection("person"),Person("Bartholomew","Tables",14)))
    //  deleteAll(getCollection("person"))
    //  updatePerson(getCollection("person"),new ObjectId("5e67878c6ee1165b7670d2d3"),Person("Robert","Tables",40))
    //  println(getPeople(getCollection("person")))





  println("abc".toInt)
}
