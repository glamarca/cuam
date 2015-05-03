/*
 * Copyright (c) 2015. Gaëtan La Marca
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers.group

import models.dao.group.{groupDao, permissionGroupDao, userGroupDao}
import models.dao.permission.permissionDao
import models.entity.group.Group
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller}
import views.html.group._

object GroupManagement extends Controller {

  /**
   * Form that handle "finding group"
   */
  val searchGroupForm = Form(
    mapping(
      "name" -> default(optional(text), None),
      "refNale" -> default(optional(text), None)
    )(SearchGroupForm.apply)(SearchGroupForm.unapply)
  )

  /**
   * The form that handle the creation and the update of a group
   */
  val groupForm = Form(
    mapping(
      "id" -> default(optional(number), None),
      "name" -> nonEmptyText,
      "refName" -> nonEmptyText,
      "creationDate" -> default(sqlDate, null),
      "updateDate" -> default(sqlDate, new java.sql.Date(new java.util.Date().getTime)),
      "updatingUser" -> default(text, "user"),
      "descritpion" -> default(optional(text), None)
    )(Group.apply)(Group.unapply)
  )

  /**
   * Show the index page of groups management
   */
  def groupIndex = Action { implicit request =>
    Ok(groupManagementView(searchGroupForm,None))
  }

  /**
   * Show the group view
   * @param id The id used to retrieve the group informations
   * @return The view page filled with the informations.
   */
  def showGroup(id: Int) = DBAction { implicit request =>
    val group = groupDao.findById(id).first
    val permissions = permissionGroupDao.findPermissions(id).list
    val users = userGroupDao.findUsers(id).list
    val allPermissions = permissionDao.dao.permissions.list
    Ok(groupView(group, Some(permissions), Some(users),Some(allPermissions)))
  }

  /**
   * Show the group form view.
   * If the id isDefined ,it is an update :
   * - The form is filled with the informations retrieved with the id
   *
   * If the id is not defined, it is a creation :
   * - The form is initialised as an empty form.
   * @param id The id of the group to update , None if creation
   * @return The form view with the form initialized.
   */
  def showGroupForm(id: Option[Int]) = DBAction { implicit request =>
    if (id.isDefined) {
      val group = groupDao.findById(id.get).first
      val filledGroupForm = groupForm.fill(group)
      Ok(groupFormView(filledGroupForm)(Messages("groupUpdate")))
    }
    else {
      Ok(groupFormView(groupForm)(Messages("groupCreation")))
    }
  }

  /**
   * Find a group according tho the searchFrom informations
   * @return The index page with the list of the found groups.
   */
  def findGroup = DBAction { implicit request =>
    searchGroupForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      validForm => {
        val groupsList = groupDao.findBySearchForm(validForm).list
        Ok(groupManagementView(searchGroupForm.fill(validForm),Some(groupsList)))
      }
    )
  }

  /**
   * Create a group using the groupForm.
   *
   * If the validation of the form fails, a message is sent to the user
   * If the name or ref name of the group already exists, a message is sent to the user.
   *
   * @return The group view filled with the new group
   */
  def createGroup = DBAction { implicit request =>
    groupForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(groupFormView(formWithErrors)(Messages("groupCreation")))
      },
      validForm => {
        val groupExists = !groupDao.findByNameOrRefName(validForm.name, validForm.refName).list.isEmpty
        if (groupExists) {
          BadRequest(groupFormView(groupForm.fill(validForm).withGlobalError(Messages("groupExists")))(Messages("groupCreation")))
        }
        else {
          val group = Group(validForm.id, validForm.name, validForm.refName, new java.sql.Date(new java.util.Date().getTime), validForm.updateDate, validForm.updatingUser, validForm.description)
          groupDao.dao.groups += group
          Redirect(routes.GroupManagement.showGroup(groupDao.findByRefName(validForm.refName).first.id.get))
        }
      }
    )
  }

  /**
   * Update the infos of a group.
   * If the name or the ref name is changed, and the one of the new ones is already in use, a message is sent to the user.
   * @return The group view filled with the updated user
   */
  def updateGroup = DBAction {implicit request =>
    groupForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(groupFormView(formWithErrors)(Messages("groupUpdate")))
      },
      validForm => {
        val groupExists = groupDao.findByNameOrRefName(validForm.name, validForm.refName).list
        if (!groupExists.isEmpty && groupExists(0).id != validForm.id) {
          BadRequest(groupFormView(groupForm.fill(validForm).withGlobalError(Messages("groupExists")))(Messages("groupUpdate")))
        }
        else {
          groupDao.findById(validForm.id.get)
            .map(g => (g.name,g.refName,g.descritpion,g.updatingUser,g.updateDate))
          .update((validForm.name,validForm.refName,validForm.description.orNull,validForm.updatingUser,validForm.updateDate))
          Redirect(routes.GroupManagement.showGroup(validForm.id.get))
        }
      }
    )
  }

  /**
   * Delete a selected group
   * @param id The id of the group to delete
   * @return The group index page
   */
  def deleteGroup(id : Int) = DBAction { implicit request =>
    groupDao.findById(id).mutate(_.delete)
    Redirect(routes.GroupManagement.groupIndex())
  }
}
