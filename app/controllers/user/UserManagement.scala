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

import models.dao.fileStorage.documentUserDao
import models.dao.user.userDao
import models.entity.user.User
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller}
import util.fileStorage
import views.html.user._
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.Json
import controllers.document.DocumentManagement._


object UserManagement extends Controller {

  val userForm = Form(
    mapping(
      "userId" -> default(optional(number), None),
      "userName" -> default(optional(text), None),
      "lastName" -> default(optional(text), None),
      "firstName" -> default(optional(text), None),
      "password" -> default(optional(text), None),
      "email" -> default(optional(email), None),
      "pictureFileName" -> default(optional(text),None)
    )(UserForm.apply)(UserForm.unapply)
  )

  val passwordForm = Form(
  mapping(
    "userId" -> number,
    "oldPassword" -> default(optional(text), None),
    "newPassword" -> default(optional(text), None)
  )(PasswordForm.apply)(PasswordForm.unapply)
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
    val userProfileForm = UserForm(user.id, Some(user.userName), user.lastName, user.firstName, None, Some(user.email),None)
    Ok(userView(userProfileForm))
  }


  def showPasswordForm(userId : Int) = Action { implicit request =>
    Ok(userPasswordFormView(passwordForm.bind(Map("userId" -> userId.toString))))
  }

  /**
   * Update the user password.
   * If the oldPassord is not correct , a message is sent to the user
   * @return The user view
   */
  def updatePassword = DBAction { implicit request =>
    passwordForm.bindFromRequest.fold(
      formWithError => {
        BadRequest(userPasswordFormView(formWithError))
      },
      form => {
        val user = userDao.findById(form.userId).first
        if(!BCrypt.checkpw(form.oldPassword.get,user.password)){
          BadRequest(userPasswordFormView(passwordForm.fill(form).withGlobalError(Messages("badOldPassword"))))
        }
        else {
          userDao.findById(form.userId).map(u => (u.password,u.updateDate,u.updatingUser))
            .update((BCrypt.hashpw(form.newPassword.get, BCrypt.gensalt),new java.sql.Date(new java.util.Date().getTime()),user.userName))
          Redirect(routes.UserManagement.showUser(form.userId))
        }
      }
    )
  }

  def uploadPicture = Action(parse.multipartFormData){implicit request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = fileStorage.getHashedName(picture.filename)
      picture.ref.moveTo(new File(s"/tmp/picture/$filename"))
      val userFormTemp = userForm.bindFromRequest.get.copy(userPicture = Some(filename))
      val action = if(userFormTemp.userId.isDefined) Messages("updateUser") else Messages("userCreation")
      Ok(userFormView(userForm.fill(userFormTemp))(action))
    }.getOrElse(BadRequest)
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
      val form = userForm fill (UserForm(user.id, Some(user.userName), user.lastName, user.firstName, None, Some(user.email),None))
      Ok(userFormView(form)(Messages("userUpdate")))
    }
    else {
      Ok(userFormView(userForm)(Messages("userCreation")))
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
        BadRequest(userFormView(formWithErrors)(Messages("userUpdate")))
      },
      form => {
        val userExist = userDao.userNameOrEmaiMatch(form.userName.get, form.email.get).list
        if (!userExist.isEmpty && userExist(0).id != form.userId) {
          BadRequest(userFormView(userForm.fill(form).withGlobalError(Messages("userExists")))(Messages("userUpdate")))
        }
        else {
          val user = (userDao.findById(form.userId.get)).list.head
          if(documentUserDao.findDocumentByUserAndDocumentName(user.id.get,Messages("document.qrCode")).list.isEmpty)createUserQrCode(user.id.get,user.userName)
          val userPictureDocument = documentUserDao.findDocumentByUserAndDocumentName(user.id.get,Messages("document.userPicture")).list
          if(userPictureDocument.isEmpty) createUserPicture(form.userId.get,form.userPicture)
          else if(userPictureDocument.head.fileName != form.userPicture.getOrElse(false)) updatePicture(userPictureDocument.head,form.userPicture)
          userDao.findById(form.userId.get).map(u => (u.userName, u.lastName, u.firstName, u.email, u.updateDate, u.updatingUser))
            .update((form.userName.get, form.lastName.get, form.firstName.get, form.email.get, new java.sql.Date(new java.util.Date().getTime()), "user"))
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
        BadRequest(userFormView(formWithErrors)(Messages("userCreation")))
      },
      form => {
        val userExist = userDao.userNameOrEmaiMatch(form.userName.get, form.email.get).list
        if (!userExist.isEmpty) {
          BadRequest(userFormView(userForm.fill(form).withGlobalError(Messages("userExists")))(Messages("userCreation")))
        }
        else {
          val date = new java.sql.Date(new java.util.Date().getTime())
          userDao.dao.users += User(None, form.userName.get, BCrypt.hashpw(form.password.get, BCrypt.gensalt), form.lastName, form.firstName, form.email.get, date, date, "user")
          val newUser = userDao.findByUserName(form.userName.get).first
          createUserQrCode(newUser.id.get,newUser.userName)
          createUserPicture(newUser.id.get,form.userPicture)
          Redirect(routes.UserManagement.showUser(newUser.id.get))
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
  def userLastNameCompletion(lastNameCandidate: String) = DBAction { implicit request =>
    val lastNamesList = userDao.findLastNameLike(lastNameCandidate).list
    Ok(Json.toJson(lastNamesList))
  }

  def userFirstNameCompletion(firstNameCandidate: String) = DBAction { implicit request =>
    val firstNamesList = userDao.findFirstNameLike(firstNameCandidate).list
    Ok(Json.toJson(firstNamesList))
  }

  def findUserNameByLastAndFirstNames(lastName: String, firstName: String) = DBAction { implicit request =>
    val userNameList = userDao.findUserNameByLastAndFirstNames(lastName, firstName).list
    Ok(Json.toJson(userNameList))
  }

  def findUserLastNameAndFirstNameByUserName(userName: String) = DBAction { implicit request =>
    val userLastAndFirstNameTuple = userDao.findByUserName(userName).map(u => (u.lastName, u.firstName)).first
    Ok(Json.toJson(Json.arr(List(userLastAndFirstNameTuple._1, userLastAndFirstNameTuple._2))))
  }

  def testAutoCompletion(str: String) = Action { implicit request =>
    val listeString = "CUAM" :: "APP2" :: Nil
    Ok(Json.toJson(listeString))
  }
}
