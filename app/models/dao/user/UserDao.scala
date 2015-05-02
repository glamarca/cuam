package models.dao.user

import controllers.user.UserForm
import models.entity.user.UserComponent
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{Config, Profile}

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery

class UserDao(override val profile: JdbcProfile) extends UserComponent with Profile {
  val users = TableQuery[Users]
}

object userDao {
  val dao = new UserDao(Config.driver)

  def search(userName: Column[Option[String]],
                 password: Column[Option[String]],
                 lastname: Column[Option[String]],
                 firstName: Column[Option[String]],
                 email: Column[Option[String]]) =
    dao.users filter { u =>
      Case.If(password.isDefined).Then(u.password === password).Else(Some(true)) &&
        Case.If(userName.isDefined).Then(u.userName === userName).Else(Some(true)) &&
        Case.If(lastname.isDefined).Then(u.lastname === lastname).Else(Some(true)) &&
        Case.If(firstName.isDefined).Then(u.firstName === firstName).Else(Some(true)) &&
        Case.If(email.isDefined).Then(u.email === email).Else(Some(true))
    }

  def searchByUserEmailPassword(userName : Column[Option[String]],email : Column[Option[String]],password : Column[String]) =
  dao.users filter { u =>
      Case.If(userName.isDefined).Then(u.userName === userName).Else(Some(true)) ||
      Case.If(email.isDefined).Then(u.email === email).Else(Some(true)) &&
        (u.password === password)
  }

  def searchByForm(userForm: UserForm) = search(userForm.userName, userForm.password,userForm.lastName,userForm.firstName,userForm.email)

  def searchById(id: Int) = dao.users filter (u => u.id === id)

  def serachByUserNameOrEmailAndPassword(userName : Option[String],eMail : Option[String],password : String) = searchByUserEmailPassword(userName,eMail,password)

}
