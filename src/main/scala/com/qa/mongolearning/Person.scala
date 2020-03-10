package com.qa.mongolearning

import org.bson.types.ObjectId

case class Person(firstName: String, surname: String, age: Int, id: ObjectId = ObjectId.get())
