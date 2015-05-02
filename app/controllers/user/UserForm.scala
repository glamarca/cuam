package controllers.user

case class UserForm(userId: Option[Int], userName: Option[String], lastName: Option[String], firstName: Option[String], password: Option[String], email: Option[String])

