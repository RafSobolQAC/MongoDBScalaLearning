package com.qa.mongolearning

import java.io.{ByteArrayInputStream, StringReader}
import org.scalatest.concurrent.ScalaFutures._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.{InjectMocks, MockitoAnnotations, Spy}
import org.mongodb.scala.{Completed, MongoCollection, Observable, Observer}
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.BeforeAndAfter

import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

class Tests extends UnitSpec with BeforeAndAfter {
  //  var services = new PersonServices
  val services: PersonServices = new PersonServices(true)
  var mockConnector: Connector = mock(classOf[Connector])
  var mockPerson: Person = mock(classOf[Person])
  var mockOutputParser: OutputParser = mock(classOf[OutputParser])
  var mockDocument: Document = mock(classOf[Document])
  var mockCollection: MongoCollection[Document] = mock(classOf[MongoCollection[Document]])
  var mockObservable: Observable[Completed] = mock(classOf[Observable[Completed]])
  var spiedServices: PersonServices = spy(services)
  var mockConnectorFactory: ConnectorFactory = mock(classOf[ConnectorFactory])
  var mockObserver: Observer[Completed] = mock(classOf[Observer[Completed]])
  var mockFutureCompleted: Future[Completed] = mock(classOf[Future[Completed]])

  val outputParser: OutputParser = new OutputParser(mockConnector)

  before {
    doReturn(mockConnector).when(mockConnectorFactory).getConnector
    spiedServices = spy(services)
  }

//  "The create person function" should "be able to fail" in {
//
//    when(mockOutputParser.getPersonDocument(any())).thenReturn(mockDocument)
//    when(mockCollection.insertOne(any())).thenReturn(mockObservable)
//    var mockFutureSeqCompleted = mock(classOf[Future[Seq[Completed]]])
//    when(mockObservable.toFuture()).thenReturn(mockFutureSeqCompleted)
//    whenReady(mockFutureSeqCompleted)
//    //    spiedServices.insertPerson(mockCollection, mockPerson)
//    assert(spiedServices.insertPerson(mockCollection, Person("a", "b", 31)))
//  }
//  it should "return Some(Person) on success" in {
//    when(mockOutputParser.getPersonDocument(any())).thenReturn(mockDocument)
//    when(mockCollection.insertOne(any())).thenReturn(mockObservable)
//    when(mockObservable.toFuture())
//      .thenReturn(Future.successful(Seq(Completed.apply())))
//    assert(spiedServices.insertPerson(mockCollection,Person("a","b",31)).isDefined)
//  }

}
