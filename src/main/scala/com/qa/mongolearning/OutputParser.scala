package com.qa.mongolearning

import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.mongodb.scala._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class OutputParser(connector: Connector) {

  def getCollection(collectionName: String): MongoCollection[Document] = {
    Thread.sleep(1000)
    Await.result(Future {
      connector.database.getCollection(s"$collectionName")
    }, 10 seconds)
  }

  def getPersonDocument(person: Person): Document = {
    Document("_id" -> person.id, "firstName" -> person.firstName, "surname" -> person.surname, "age" -> person.age)
  }

  def getPersonDocumentNoId(person: Person): Document = {
    Document("firstName" -> person.firstName, "surname" -> person.surname, "age" -> person.age)
  }

  def makeFilterId(objectId: ObjectId): Bson = {
    Document("_id" -> objectId)
  }

  def makeFilterNoId(person: Person): Bson = {
    Document("firstName" -> person.firstName, "surname" -> person.surname, "age" -> person.age)
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
  def makePersonFromCollectionFind(document: Document): Person = {
    Person(getString(document, "firstName"),
      getString(document, "surname"),
      getInt(document, "age"),
      getObjectId(document, key = "_id")
    )
  }

}
