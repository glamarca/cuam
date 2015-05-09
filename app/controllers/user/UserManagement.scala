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
import play.api.libs.json.Json


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

  /**
   * Index page of the users management
   * @return The serach page for users.
   */
  def userIndex = Action { implicit request =>
    Ok(userManagementView(userForm, None))
  }

  /**
   * Show the user view fillled with the user informations found with the user id.
   * @param id The id of the user we look for
   * @return The user view filled with the user information
   */
  def showUser(id: Int) = DBAction { implicit request =>
    val user = (userDao.findById(id)).list.head
    val userProfileForm = UserForm(user.id, Some(user.userName), user.lastName, user.firstName, None, Some(user.email))
    Ok(userView(userProfileForm))
  }

  /**
   * Show the form used to create or update a user.
   *
   * If the id is not defined , the creation form is sent :
   * - The password field is shown and mandatory
   * - The action is set to creation
   * - The form is initialized to an empty form
   *
   * If the id is defined , the update form is sent :
   * - The password field is not rendered
   * - The action is set to update
   * - The form is filled with the user information retrieved with the user id
   *
   * @param id The id of the user if not new , none if new
   * @return The user form initilised for the action
   */
  def showUserForm(id: Option[Int]) = DBAction { implicit request =>
    if (id.isDefined) {
      val user = (userDao.findById(id.get)).list.head
      val form = userForm fill (UserForm(user.id, Some(user.userName), user.lastName, user.firstName, None, Some(user.email)))
      Ok(UserFormView(form)(Messages("userUpdate")))
    }
    else {
      Ok(UserFormView(userForm)(Messages("userCreation")))
    }
  }

  /**
   * Update a user with the informations in the form.
   * If the userName or the email are modified and the new ones are already in use , a message is sent to the user.
   * @return The user view filled with the updated user informations.
   */
  def updateUser = DBAction { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(UserFormView(formWithErrors)(Messages("userUpdate")))
      },
      form => {
        val userExist = userDao.userNameOrEmaiMatch(form.userName.get, form.email.get).list
        if (!userExist.isEmpty && userExist(0).id != form.userId) {
          BadRequest(UserFormView(userForm.fill(form).withGlobalError(Messages("userExists")))(Messages("userUpdate")))
        }
        else {
          val user = (userDao.findById(form.userId.get)).list.head
          userDao.findById(form.userId.get).map(u => (u.userName,u.lastName,u.firstName,u.email,u.updateDate,u.updatingUser))
            .update((form.userName.get,form.lastName.get,form.firstName.get,form.email.get,new java.sql.Date(new java.util.Date().getTime()), "user"))
          Redirect(routes.UserManagement.showUser(form.userId.get))
          }
      }
    )
  }

  /**
   * Create a user according to the form fields.
   * If the userName or the email is alreay in use , a message is send to the user.
   * The password is encrypted using JBCrypt
   * @return The user view filled with the new user informations.
   */
  def createUser = DBAction { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(UserFormView(formWithErrors)(Messages("userCreation")))
      },
      form => {
        val userExist = userDao.userNameOrEmaiMatch(form.userName.get, form.email.get).list
        if (!userExist.isEmpty) {
          BadRequest(UserFormView(userForm.fill(form).withGlobalError(Messages("userExists")))(Messages("userCreation")))
        }
        else {
          val date = new java.sql.Date(new java.util.Date().getTime())
          userDao.dao.users += User(None, form.userName.get, BCrypt.hashpw(form.password.get, BCrypt.gensalt), form.lastName, form.firstName, form.email.get, date, date, "user")
          Redirect(routes.UserManagement.showUser(userDao.findByUserName(form.userName.get).first.id.get))
        }
      }
    )
  }

  /**
   * Find user according to the form informations.
   * @return The user search page with the list of users which were found.
   */
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

  /**
   * Delete a user
   * @param id The id of the user to delete
   * @return The user search page
   */
  def deleteUser(id: Int) = DBAction { implicit request =>
    userDao.dao.users.filter(_.id === id).mutate(_.delete)
    Ok(userManagementView(userForm, None))
  }


  /**
   * Find the list of users when user.name like $lastNameCandidate
   * @param lastNameCandidate
   * @return
   */
  def userLastNameCompletion(lastNameCandidate : String) = DBAction { implicit request =>
      val lastNamesList = userDao.findLastNameLike(lastNameCandidate).list
      Ok(Json.toJson(lastNamesList))
  }

  def userFirstNameCompletion(firstNameCandidate : String) = DBAction { implicit request =>
      val firstNamesList = userDao.findFirstNameLike(firstNameCandidate).list
      Ok(Json.toJson(firstNamesList))
  }

  def findUserNameByLastAndFirstNames(lastName : String , firstName : String)  = DBAction { implicit request =>
    val userNameList = userDao.findUserNameByLastAndFirstNames(lastName,firstName).list
    Ok(Json.toJson(userNameList))
  }

  def findUserLastNameAndFirstNameByUserName(userName : String) = DBAction { implicit request =>
    val userLastAndFirstNameTuple = userDao.findByUserName(userName).map(u => (u.lastName,u.firstName)).first

    Ok(Json.toJson(Json.arr(List(userLastAndFirstNameTuple._1,userLastAndFirstNameTuple._2))))
  }

  def testAutoCompletion(str : String) = Action { implicit request =>
    val listeString = "CUAM"::"APP2"::Nil
    Ok(Json.toJson(listeString))
  }

}
