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

package models.dao.application

import models.entity.application.ApplicationComponent
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{Config, Profile}

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery

class ApplicationDao(override val profile: JdbcProfile) extends ApplicationComponent with Profile {
  val applications = TableQuery[Applications]
}

object applicationDao {
  val dao = new ApplicationDao(Config.driver)

  /**
   *
   * Generic query to find a application with name and refName
   * @param name The name of the app
   * @param refName The reference name of the app
   * @return The query with optional parameters
   */
  def find(name : Column[Option[String]],refName : Column[Option[String]]) =
      dao.applications filter (app => {
        Case.If(name.isDefined).Then(app.name === name).Else(Some(true)) &&
        Case.If(refName.isDefined).Then(app.refName === refName)
      })

  /**
   * Find an application using the searchForm
   * @param searchForm The searchForm
   * @return The parametrized query
   */
  def findBySearchForm(searchForm : (Option[String],Option[String])) = find(searchForm._1,searchForm._2)

  /**
   * Find an application by its ID
   * @param id The application ID
   * @return The query to find the application
   */
  def findById(id : Int) = dao.applications filter (_.id === id)

  /**
   * Find an application by its refName
   * @param refName The application refName
   * @return The query to find the application
   */
  def findByRefName(refName : String) = dao.applications filter (_.refName === refName)

  /**
   * Find a list of applications that matches either the name of the refName
   * @param name The name of the application
   * @param refName The refName of the application
   * @return The query to fn-ind the list of applications
   */
  def findByNameOrRefName(name : String,refName : String) = dao.applications filter (app => app.name === name || app.refName === refName)
}


