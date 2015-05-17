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

import models.dao.application.applicationDao
import models.dao.group.{groupDao, permissionGroupDao, userGroupDao}
import models.dao.permission.permissionDao
import models.dao.user.userDao
import models.entity.group.{Group, PermissionGroup, UserGroup}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller}
import views.html.group._

import scala.collection.mutable.ListBuffer

object GroupManagement extends Controller {

  def  allApplicationsList = DB.withSession { implicit request =>
    applicationDao.dao.applications.list
  }

  /**
   * Form that handle "finding group"
   */
  val searchGroupForm = Form(
    mapping(
      "name" -> default(optional(text), None),
      "refNale" -> default(optional(text), None),
      "applicationID" -> default(optional(number), None)
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
      "description" -> default(optional(text), None),
      "applicationId" -> number
    )(Group.apply)(Group.unapply)
  )

  /**
   * Show the index page of groups management
   */
  def groupIndex = Action { implicit request =>
    Ok(groupManagementView(searchGroupForm, None, allApplicationsList))
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
    Ok(groupView(group, Some(permissions), Some(users), Some(allPermissions),None))
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
      Ok(groupFormView(filledGroupForm, allApplicationsList)(Messages("groupUpdate")))
    }
    else {
      Ok(groupFormView(groupForm, allApplicationsList)(Messages("groupCreation")))
    }
  }

  /**
   * Find a group according tho the searchFrom informations
   * @return The index page with the list of the found groups.
   */
  def findGroup = DBAction { implicit request =>
    searchGroupForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(groupManagementView(formWithErrors, None, allApplicationsList))
      },
      validForm => {
        val groupsList = groupDao.findBySearchForm(validForm).list
        Ok(groupManagementView(searchGroupForm.fill(validForm), Some(groupsList), allApplicationsList))
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
        BadRequest(groupFormView(formWithErrors, allApplicationsList)(Messages("groupCreation")))
      },
      validForm => {
        val groupExists = !groupDao.findByNameOrRefName(validForm.name, validForm.refName).list.isEmpty
        if (groupExists) {
          BadRequest(groupFormView(groupForm.fill(validForm).withGlobalError(Messages("groupExists")), allApplicationsList)(Messages("groupCreation")))
        }
        else {
          val group = Group(validForm.id, validForm.name, validForm.refName, new java.sql.Date(new java.util.Date().getTime), validForm.updateDate, validForm.updatingUser, validForm.description, validForm.applicationId)
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
  def updateGroup = DBAction { implicit request =>
    groupForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(groupFormView(formWithErrors, allApplicationsList)(Messages("groupUpdate")))
      },
      validForm => {
        val groupExists = groupDao.findByNameOrRefName(validForm.name, validForm.refName).list
        if (!groupExists.isEmpty && groupExists(0).id != validForm.id) {
          BadRequest(groupFormView(groupForm.fill(validForm).withGlobalError(Messages("groupExists")), allApplicationsList)(Messages("groupUpdate")))
        }
        else {
          groupDao.findById(validForm.id.get)
            .map(g => (g.name, g.refName, g.descritpion, g.updatingUser, g.updateDate, g.applicationId))
            .update((validForm.name, validForm.refName, validForm.description.orNull, validForm.updatingUser, validForm.updateDate, validForm.applicationId))
          Redirect(routes.GroupManagement.showGroup(validForm.id.get))
        }
      }
    )
  }

  /**
   * Delete a selected group
   * If the group contains user or permission ,
   * a message is sent to the user to remove them from the group before deletion
   * @param id The id of the group to delete
   * @return The group index page
   */
  def deleteGroup(id: Int) = DBAction { implicit request =>
    val permissionGroupListIsEmpty = permissionGroupDao.findPermissions(id).list.isEmpty
    val userGroupListIsEmpty = userGroupDao.findUsers(id).list.isEmpty
    if (!permissionGroupListIsEmpty || !userGroupListIsEmpty) {
      val group = groupDao.findById(id).first
      val permissions = permissionGroupDao.findPermissions(id).list
      val users = userGroupDao.findUsers(id).list
      val listeMessage = {
        var messages = ListBuffer[String]()
        if(!permissionGroupListIsEmpty) messages += Messages("groupStillContainsPermissions")
        if (!userGroupListIsEmpty) messages += Messages("groupStillContainsUsers")
        messages.toList
      }
      val allPermissions = permissionDao.dao.permissions.list
      BadRequest(groupView(group, Some(permissions), Some(users), Some(allPermissions),Some(listeMessage)))
    }
    else {
      groupDao.findById(id).mutate(_.delete)
      Redirect(routes.GroupManagement.groupIndex())
    }
  }

  /**
   * Add a permission to a group
   * @param groupId The id of the group
   * @param permissionId The id of the permission we want to add
   * @return The group view with updated data
   */
  def addPermissionToGroup(groupId: Int, permissionId: Int) = DBAction { implicit request =>
    permissionGroupDao.dao.permissionsGroups += PermissionGroup(None, permissionId, groupId, new java.sql.Date(new java.util.Date().getTime))
    Redirect(routes.GroupManagement.showGroup(groupId))
  }

  /**
   * Add a user to a group by using his userName or last and firstName
   * @param groupId The id of the group
   * @param userLastName The lastName of the user
   * @param userFirstName The firstName of the user
   * @param userUserName The userName of the user
   * @return The group view with data updated.
   */
  def addUserToGroup(groupId: Int, userLastName: Option[String], userFirstName: Option[String], userUserName: Option[String]) = DBAction { implicit request =>
    val user = {
      if (userUserName.isDefined && !userUserName.isEmpty) Some(userDao.findByUserName(userUserName.get).first)
      else if (userLastName.isDefined && !userLastName.isEmpty && userFirstName.isDefined && !userFirstName.isEmpty) Some(userDao.findByUserLastAndFirstName(userLastName.get, userFirstName.get).first)
      else None
    }
    if (user.isDefined) {
      userGroupDao.dao.usersgroups += UserGroup(None, user.get.id.get, groupId, new java.sql.Date(new java.util.Date().getTime))
    }
    Redirect(routes.GroupManagement.showGroup(groupId))
  }

  /**
   * Remove a permission from a group
   * @param groupId The id of the group
   * @param permissionId The id of the permission we want to remove
   * @return The group view with updated data
   */
  def removePermissionFromGroup(groupId: Int, permissionId: Int) = DBAction { implicit request =>
    permissionGroupDao.findByPermissionAndGroupIds(permissionId, groupId).mutate(_.delete)
    Redirect(routes.GroupManagement.showGroup(groupId))
  }

  /**
   * Remove a user from a group
   * @param groupId The id of the group
   * @param userId The id of the user we want to remove from the group
   * @return The group view with updated data.
   */
  def removeUserFromGroup(groupId: Int, userId: Int) = DBAction { implicit request =>
    userGroupDao.findByUserAndGroupIds(userId, groupId).mutate(_.delete)
    Redirect(routes.GroupManagement.showGroup(groupId))
  }
}
