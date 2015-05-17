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
package controllers.permission

import java.util.Locale

import models.dao.application.applicationDao
import models.dao.group.permissionGroupDao
import models.dao.permission.permissionDao
import models.entity.permission.Permission
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller}
import views.html.permission._

object PermissionManagement extends Controller {

  val allApplicationsList = DB.withSession {implicit request =>
      applicationDao.dao.applications.list
  }

  /**
   * Le formulaire de creation et modification d'une permission
   */
  val permissionForm = Form(
    mapping(
      "id" -> default(optional(number), None),
      "name" -> nonEmptyText,
      "refName" -> nonEmptyText,
      "creationDate" -> default(sqlDate, null),
      "description" -> default(optional(text), None),
      "modificationDate" -> default(sqlDate, null),
      "updatingUser" -> default(text, "user"),
      "applicationId" -> number
    )(Permission.apply)(Permission.unapply)
  )

  /**
   * Le formualire de recherche d'une permission
   */
  val searchForm = Form(
    mapping(
      "name" -> default(optional(text), None),
      "refName" -> default(optional(text), None),
      "applicationId" -> default(optional(number), None)
    )(SearchPermissionForm.apply)(SearchPermissionForm.unapply)
  )

  /**
   * The index page of the permission management
   */
  def permissionIndex = Action { implicit request =>
    Ok(permissionManagementView(None,allApplicationsList))
  }

  /**
   * Find a list of permissions with the criteria's provided by the search form
   * @return The search view with the list of the permissions that where found
   */
  def findPermission = DBAction { implicit request =>
    searchForm.bindFromRequest.fold(
      formWithError => {
        BadRequest(permissionManagementView(None,allApplicationsList))
      },
      searchForm => {
        val permissionsList = permissionDao.findBySearchForm(searchForm).list
        Ok(permissionManagementView(Some(permissionsList),allApplicationsList))
      }
    )
  }

  /**
   * Show the permission view.
   * The view is filled with the information retrievd wit the permission id
   */
  def showPermission(id: Int) = DBAction { implicit request =>
    val permission = permissionDao.findById(id).first
    val groupsList = permissionGroupDao.findGroups(id).list
    Ok(permissionView(permission, Some(groupsList)))
  }

  /**
   * Update a permission with the informations provided by the form.
   * If the name of the reference name of the permission were updated  and the new ones are already in use , a message is sent to the user.
   * @return The permission view filled with the updated permissions informations.
   */
  def updatePermission = DBAction { implicit request =>
    permissionForm.bindFromRequest.fold(
      formWithError => {
        BadRequest(permissionFormView(formWithError,allApplicationsList)(Messages("permissionUpdate")))
      },
      form => {
        val permissionExists = permissionDao.findByNameOrRefName(form.name, form.refName).list
        if (!permissionExists.isEmpty && permissionExists(0).id != form.id) {
          BadRequest(permissionFormView(permissionForm.fill(form).withGlobalError(Messages("permissionExist")),allApplicationsList)(Messages("permissionUpdate")))
        }
        else {
          permissionDao.findById(form.id.get).map(p => (p.name, p.refName, p.description, p.modificationDate, p.updatingUser))
            .update((form.name, form.refName.toUpperCase(Locale.ENGLISH), form.description.orNull, new java.sql.Date(new java.util.Date().getTime), "user"))
          Redirect(routes.PermissionManagement.showPermission(form.id.get))
        }
      }
    )
  }

  /**
   * Create a new permission with the informations provided by the form.
   * If a permission with the same name or reference name already exists , a message is sent to the user.
   * @return The permission view filled with the new permissions informations.
   */
  def createPermission = DBAction { implicit request =>
    permissionForm.bindFromRequest.fold(
      formWithError => {
        BadRequest(permissionFormView(formWithError,allApplicationsList)(Messages("permissionCreation")))
      },
      form => {
        if (!permissionDao.findByNameOrRefName(form.name, form.refName).list.isEmpty) {
          BadRequest(permissionFormView(permissionForm.fill(form).withGlobalError(Messages("permissionExist")),allApplicationsList)(Messages("permissionCreation")))
        }
        else {
          val permissionWithDate = Permission(None, form.name, form.refName.toUpperCase(Locale.ENGLISH), new java.sql.Date(new java.util.Date().getTime()), form.description, new java.sql.Date(new java.util.Date().getTime()), "user",form.applicationId)
          permissionDao.dao.permissions += permissionWithDate
          Redirect(routes.PermissionManagement.showPermission(permissionDao.findByNameOrRefName(form.name, form.refName).first.id.get))
        }
      }
    )
  }

  /**
   * Delete a permission identified by is ID.
   * Also delete all links to the permission Table (permissionGroup,...)
   * @param id The id of the permission to delete.
   * @return The index page of the permission management.
   */
  def deletePermission(id: Int) = DBAction { implicit request =>
    permissionGroupDao.findPermissions(id).mutate(_.delete)
    permissionDao.findById(id).mutate(_.delete)
    Redirect(routes.PermissionManagement.permissionIndex())
  }

  /**
   * Show the permission form.
   *
   * If the id is defined , the form is filled with the informations retrieved by the id
   * If the id is not defined , the form is initialized as an empty form.
   *
   * @param id The id if it is an uodate , None if it is a creation
   * @return The permission form view
   */
  def showPermissionForm(id: Option[Int]) = DBAction { implicit request =>
    if (id.isDefined) {
      val permission = permissionDao.findById(id.get).first
      Ok(permissionFormView(permissionForm.fill(permission),allApplicationsList)(Messages("permissionUpdate")))
    }
    else {
      Ok(permissionFormView(permissionForm,allApplicationsList)(Messages("permissionCreation")))
    }
  }

}
