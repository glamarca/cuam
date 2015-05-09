/*
Copyright 2015 Gaëtan La Marca

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/**
 * @author
 */
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

  def find(userName: Column[Option[String]],
                 password: Column[Option[String]],
                 lastname: Column[Option[String]],
                 firstName: Column[Option[String]],
                 email: Column[Option[String]]) =
    dao.users filter { u =>
      Case.If(password.isDefined).Then(u.password === password).Else(Some(true)) &&
        Case.If(userName.isDefined).Then(u.userName === userName).Else(Some(true)) &&
        Case.If(lastname.isDefined).Then(u.lastName === lastname).Else(Some(true)) &&
        Case.If(firstName.isDefined).Then(u.firstName === firstName).Else(Some(true)) &&
        Case.If(email.isDefined).Then(u.email === email).Else(Some(true))
    }

  def findByForm(userForm: UserForm) = find(userForm.userName, userForm.password,userForm.lastName,userForm.firstName,userForm.email)

  def findById(id: Int) = dao.users filter (u => u.id === id)

  def userNameOrEmaiMatch(userName : String,email : String) = dao.users filter (u => u.userName === userName || u.email === email)

  def findByUserName(userName : String) = dao.users filter (u => u.userName === userName)

  def findLastNameLike(lastNameCandidate : String) = dao.users filter (u => u.lastName like s"$lastNameCandidate%") map (u => u.lastName)

  def findFirstNameLike(firstNameCandidate : String) = dao.users filter (u => u.lastName like s"$firstNameCandidate%") map (u => u.firstName)

  def findUserNameByLastAndFirstNames(firstName : String,lastName : String) = dao.users filter (u => u.lastName === lastName && u.firstName === firstName) map (u => u.userName)


}
