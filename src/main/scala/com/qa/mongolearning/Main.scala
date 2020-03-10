package com.qa.mongolearning

import org.bson.types.ObjectId
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDocument

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object Main extends App {


  def getCredentials: String = {
    Source.fromResource("userCredentials.txt").getLines().toList.head
  }

  val mongoClient: MongoClient = MongoClient(getCredentials)

  val database: MongoDatabase = mongoClient.getDatabase("mydb")
  val collection: MongoCollection[Document] = database.getCollection("person");

  def getCollection(collectionName: String): MongoCollection[Document] = {
    database.getCollection(s"$collectionName")
  }

  def getPersonDocument(person: Person): Document = {
    Document("_id" -> ObjectId.get(), "firstName" -> person.firstName, "surname" -> person.surname, "age" -> person.age)
  }

  def insertPerson(collection: MongoCollection[Document], person: Document): Option[Document]= {
    val observable: Observable[Completed] = collection.insertOne(person)
    var returner: Option[Document] = None

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
  def getValueFromKey(entry: Document, key: String): Any = {
    entry.get(key).get.asString.getValue
  }
  def getPeople(collection: MongoCollection[Document]): Option[List[Document]] = {
//    val doc = Await.result(collection.find().first().head(), 10 seconds)
    val doc = Await.result(collection.find().toFuture(), 10 seconds)
    var returner: Option[List[Document]] = None
    var listToAdd = new ListBuffer[Any]
    println("Doc is "+doc)
    println(doc.head)
    println(doc(0).get("firstName").get.asString().getValue)
    if (doc.nonEmpty) {
      doc.foreach(el => listToAdd += Person(getValueFromKey(el,"firstName").asInstanceOf[String] ,getValueFromKey(el,"surname").asInstanceOf[String], getValueFromKey(el,"age").asInstanceOf[Int]))

    }
    println(listToAdd)
    returner
  }

//  println(insertPerson(getCollection("person"),getPersonDocument(Person("Bobby","Tables",12))))
  println(getPeople(getCollection("person")))
}
