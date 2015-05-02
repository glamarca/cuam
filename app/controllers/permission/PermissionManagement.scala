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
package controllers.permission

import java.util.Locale

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
      "updatingUser" -> default(text, "user")
    )(Permission.apply)(Permission.unapply)
  )

  /**
   * Le formualire de recherche d'une permission
   */
  val searchForm = Form(
    tuple(
      "name" -> default(optional(text), None),
      "refName" -> default(optional(text), None)
    )
  )

  /**
   * Page d'accueil de la gestion des permissions
   * @return Une liste de permissions vide dans la vue recherche
   */
  def permissionIndex = Action { implicit request =>
    Ok(permissionManagementView(None))
  }

  /**
   * Recherche une liste de permission en fonction de "fomulaireRecherche"
   * @return La Liste des permission trouvées , dans la vue de recherche
   */
  def findPermission = DBAction { implicit request =>
    searchForm.bindFromRequest.fold(
      formWithError => {
        BadRequest(permissionManagementView(None))
      },
      searchForm => {
        val permissionsList = permissionDao.findByNames(searchForm._1, searchForm._2).list
        Ok(permissionManagementView(Some(permissionsList)))
      }
    )
  }

  /**
   * Permet d'afficher la fiche d'une permission
   * @param id L'id de la permission
   * @return La vue de la fiche de permission
   */
  def showPermission(id: Int) = DBAction { implicit request =>
    val permission = permissionDao.findById(id).first
    Ok(permissionView(permission, None))
  }

  /**
   * Mise à jour de la permissiond ans la DB et renvoie de la nouvelle permission
   * @param permission La permission à mettre à jour
   * @return La permission mise à jour.
   */
  def updatePermission(permission: Permission): Permission = DB.withSession { implicit request =>
    permissionDao.findById(permission.id.get).map(p => (p.name, p.refName, p.description, p.modificationDate, p.updatingUser))
      .update((permission.name, permission.refName.toUpperCase(Locale.ENGLISH), permission.description.orNull, new java.sql.Date(new java.util.Date().getTime), "user"))
    permissionDao.findById(permission.id.get).first
  }

  /**
   * Création d'une permission dans la DB
   * @param permission La permission à créer
   * @return La fiche de la permission créée
   */
  def createPermission(permission: Permission): Permission = DB.withSession { implicit request =>
    val permissionWithDate = Permission(None, permission.name, permission.refName.toUpperCase(Locale.ENGLISH), new java.sql.Date(new java.util.Date().getTime()), permission.description, new java.sql.Date(new java.util.Date().getTime()), "user")
    permissionDao.dao.permissions += permissionWithDate
    permissionDao.findByRefName(permission.refName).first
  }

  /**
   * Permet de lancer la creation ou la modification d'une permission en fonction de "permissionForm"
   * @return La fiche de la permission crée
   */
  def updateCreatePermission = DBAction { implicit request =>
    permissionForm.bindFromRequest.fold(
      formWithError => {
        BadRequest(permissionFormView(formWithError)(Messages("formError")))
      },
      formulaire => {
        if (formulaire.id.isDefined) {
          val permissionExists = permissionDao.findByNameOrRefName(formulaire.name, formulaire.refName).list
          if (!permissionExists.isEmpty && permissionExists(0).id != formulaire.id) {
            BadRequest(permissionFormView(permissionForm.fill(formulaire).withGlobalError(Messages("permissionExist")))(Messages("permissionCreation")))
          }
          else {
            val updatedPermission = updatePermission(formulaire)
            Redirect(routes.PermissionManagement.showPermission(updatedPermission.id.get))
          }
        }
        else {
          if (!permissionDao.findByNameOrRefName(formulaire.name,formulaire.refName).list.isEmpty) {
            BadRequest(permissionFormView(permissionForm.fill(formulaire).withGlobalError(Messages("permissionExist")))(Messages("permissionCreation")))
          }
          else {
            val updatedPermission = createPermission(formulaire)
            Redirect(routes.PermissionManagement.showPermission(updatedPermission.id.get))
          }
        }
      }
    )
  }

  /**
   * Supprime une permission
   * @param id L'id de la permission à supprimer
   * @return La vue de recherche des permissions
   */
  def deletePermission(id: Int) = DBAction { implicit request =>
    permissionDao.findById(id).mutate(_.delete)
    Redirect(routes.PermissionManagement.permissionIndex())
  }

  /**
   * Affiche une fiche de permission
   * @param id L'id de la permission à afficher
   * @return La vue de la fiche de permission
   */
  def showPermissionForm(id: Option[Int]) = DBAction { implicit request =>
    if (id.isDefined) {
      val permission = permissionDao.findById(id.get).first
      Ok(permissionFormView(permissionForm.fill(permission))(Messages("permissionUpdate")))
    }
    else {
      Ok(permissionFormView(permissionForm)(Messages("permissionCreation")))
    }
  }

}
