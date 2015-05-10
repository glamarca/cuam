/*
 *
 * Copyright 2015 Gaëtan La Marca
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
 * /
 */

package controllers.application

import models.dao.application.applicationDao
import models.dao.group.groupDao
import play.api.mvc.{Action, Controller}
import views.html.application._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import models.entity.application.Application

object ApplicationManagement extends Controller{

  val applicationForm = Form(
      mapping(
        "id" -> default(optional(number),None),
        "name" -> nonEmptyText,
      "refName" -> nonEmptyText,
      "description" -> default(optional(text),None),
      "creationDate" -> default(sqlDate,null),
      "updateDate" -> default(sqlDate,new java.sql.Date(new java.util.Date().getTime)),
      "updatingUser" -> default(nonEmptyText,"user")
      )(Application.apply)(Application.unapply)
  )

  val searchForm = Form(
    tuple(
      "name" -> default(optional(text), None),
      "refName" -> default(optional(text), None)
    )
  )

  /**
   * Show the index page of applications management
   */
  def applicationIndex = Action {implicit request =>
    Ok(applicationManagementView(None))
  }

  /**
   * Show the information page of an application
   * @param id The id of the application whe are looking for
   * @return The application view filled with the retrieved informations
   */
  def showApplication(id : Int) = DBAction{implicit request =>
    val application = applicationDao.findById(id).first
    val groupsList = groupDao.findByApplicationId(id).list
    Ok(applicationView(application,Some(groupsList)))
  }

  /**
   * Show the application Form
   *
   * If the id is defined , it is an upis ormations retrieved with the id.
   *
   * If the id is not defined , it is a creation
   * - The form is initialized as an empty form
   * @param id : The possible id of the application , None if creation
   * @return The form view initialized.
   */
  def showApplicationForm(id : Option[Int]) = DBAction{implicit request =>
    if(id.isDefined){
      val application = applicationDao.findById(id.get).first
      Ok(applicationFormView(applicationForm.fill(application))(Messages("applicationUpdate")))
    }
    else{
      Ok(applicationFormView(applicationForm)(Messages("applicationCreation")))
    }
  }

  /**
   * Create a new application using the form informations
   *
   * If the name of the refName is already in use , a message is sent to the user.
   * @return The application view filled with the new application informations
   */
  def createApplication = DBAction {implicit request =>
      applicationForm.bindFromRequest.fold(
        formWithError => {
          BadRequest(applicationFormView(formWithError)(Messages("applicationCreation")))
        },
        app => {
          if(!applicationDao.findByNameOrRefName(app.name,app.refName).list.isEmpty){
            BadRequest(applicationFormView(applicationForm.fill(app).withGlobalError(Messages("applicationExists")))(Messages("applicationCreation")))
          }
          else{
            val application = Application(app.id,app.name,app.refName,app.description,new java.sql.Date(new java.util.Date().getTime),app.updateDate,app.updatingUser)
            applicationDao.dao.applications += application
            Redirect(routes.ApplicationManagement.showApplication(applicationDao.findByRefName(app.refName).first.id.get))
          }
        }
      )
  }

  /**
   * Update an existing application using the information in the form.
   *
   * If the name of the refName is changed , and the new ones are already in use , a message is sent to the user.
   *
   * @return The application view filled with the updated informations
   */
  def updateApplication = DBAction {implicit request =>
      applicationForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(applicationFormView(formWithErrors)(Messages("applicationUpdate")))
      },
      app => {
        val appsList = applicationDao.findByNameOrRefName(app.name,app.refName).list
        if(!appsList.isEmpty && appsList(0).id.get != app.id.get){
          BadRequest(applicationFormView(applicationForm.fill(app).withGlobalError(Messages("applicationExists")))(Messages("applicationCreation")))
        }
        else{
          Redirect(routes.ApplicationManagement.showApplication(app.id.get))
        }
      }
      )
  }

  /**
   * Delete an application using the id
   * @param id The id of the application we want to delete
   * @return The index page of the application management
   */
  def deleteApplication(id : Int) = DBAction{implicit request =>
    applicationDao.findById(id).mutate(_.delete)
    Redirect(routes.ApplicationManagement.applicationIndex())
  }

  /**
   * Find applications that matches the searchForm
   * @return The application index with the list of finded applications
   */
  def findApplication = DBAction {implicit request =>
    searchForm.bindFromRequest.fold(
      formWithError => {
        BadRequest(applicationManagementView(None))
      },
      searchForm => {
        val applicationsList = applicationDao.findBySearchForm(searchForm).list
        Ok(applicationManagementView(Some(applicationsList)))
      }
    )
  }
}
