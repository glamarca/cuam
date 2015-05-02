/*
Copyright 2015 La Marca Gaëtan

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
package controllers.user

import models.dao.user.userDao
import models.entity.user.User
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller}
import views.html.user._
import org.mindrot.jbcrypt.BCrypt


object UserManagement extends Controller {

  val userForm = Form(
    mapping(
      "userId" -> default(optional(number), None),
      "userName" -> default(optional(text), None),
      "lastName" -> default(optional(text), None),
      "firstName" -> default(optional(text), None),
      "password" -> default(optional(text), None),
      "email" -> default(optional(email), None)
    )(UserForm.apply)(UserForm.unapply)
  )

  def userIndex = Action { implicit request =>
    Ok(userManagementView(userForm, None))
  }

  def showUser(id: Int) = DBAction { implicit request =>
    val user = (userDao.findById(id)).list.head
    val userProfileForm = UserForm(user.id, Some(user.userName), user.lastName, user.firstName,None,Some(user.email))
    Ok(userView(userProfileForm))
  }

  def showUserForm(id: Option[Int]) = DBAction { implicit request =>
    if (id.isDefined) {
      val user = (userDao.findById(id.get)).list.head
      val form = userForm fill (UserForm(user.id, Some(user.userName), user.lastName, user.firstName,None, Some(user.email)))
      Ok(UserFormView(form)(Messages("userUpdate")))
    }
    else {
      Ok(UserFormView(userForm)(Messages("userCreation")))
    }
  }

  def addUser(form: UserForm) = DB.withSession { implicit request =>
    val date = new java.sql.Date(new java.util.Date().getTime())
    userDao.dao.users += User(None, form.userName.get,BCrypt.hashpw(form.password.get, BCrypt.gensalt), form.lastName, form.firstName, form.email.get, date, date, "user")
  }

  def updateUser(form: UserForm) = DB.withSession { implicit request =>
    val user = (userDao.findById(form.userId.get)).list.head
    val userWithDate = User(user.id, user.userName, user.password, user.lastName, user.firstName, user.email, user.creationDate, new java.sql.Date(new java.util.Date().getTime()), "user")
    userDao.dao.users.filter(_.id === form.userId).update(userWithDate)
  }

  def updateCreateUser = DBAction { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(UserFormView(formWithErrors)(Messages("userUpdate")))
      },
      form => {
        val userExist = userDao.nameOrEmaiMatch(form.userName.get,form.email.get).list
        if (!userExist.isEmpty) {
          BadRequest(UserFormView(userForm.fill(form).withGlobalError(Messages("permissionExist")))(Messages("userUpdate")))
        }
        else {
          if (form.userId.isDefined) {
            updateUser(form)
            Redirect(routes.UserManagement.showUser(form.userId.get))
          }
          else {
            addUser(form)
            Redirect(routes.UserManagement.showUser(userDao.findByUserName(form.userName.get).first.id.get))
          }
        }
      }
    )
  }

  def findUser = DBAction { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(userManagementView(formWithErrors, None))
      },
      searchForm => {
        val usersList = userDao.findByForm(searchForm).list
        Ok(userManagementView(userForm, Some(usersList)))
      }
    )
  }

  def deleteUser(id: Int) = DBAction { implicit request =>
    userDao.dao.users.filter(_.id === id).mutate(_.delete)
    Ok(userManagementView(userForm, None))
  }

}
