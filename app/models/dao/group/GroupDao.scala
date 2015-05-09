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
package models.dao.group

import controllers.group.SearchGroupForm
import models.dao.application.applicationDao
import models.entity.group.GroupComponent
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{Config, Profile}

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery


class GroupDao(override val profile: JdbcProfile) extends GroupComponent with Profile {
  val groups = TableQuery[Groups]
}

object groupDao {
  val dao = new GroupDao(Config.driver)

  /**
   * Generic find method using optionnal values
   * @param name The column mapped to group name
   * @param refName The column mapped to group refName
   * @return A generic query
   */
  def find(  name : Column[Option[String]],
            refName : Column[Option[String]],
            applicationId : Column[Option[Int]]) =
  dao.groups.filter { g =>
    Case.If(name.isDefined).Then(g.name === name).Else((Some(true))) &&
    Case.If(refName.isDefined).Then(g.refName === refName).Else(Some(true))
  }

  /**
   * Find groups with the criteria's provided by the searchForm
   * @param searchForm The search form
   * @return The query to find the groups paramerized with the search form infos.
   */
  def findBySearchForm(searchForm : SearchGroupForm) = find(searchForm.name,searchForm.refName)

  /**
   * Find a group by his id
   * @param id The id of the group
   * @return The parametrized query to find the group
   */
  def findById(id : Int) = dao.groups.filter(_.id === id)

  /**
   * Find groups that match the name or the refname
   * @param name The name of the group
   * @param refName The refName of the group
   * @return The list of groups that match name or refname
   */
  def findByNameOrRefName(name : String,refName : String) = dao.groups filter (g => g.name === name || g.refName === refName)

  /**
   * Find a permission by refname
   * @param refName The refname of the permission
   * @return The query to find the refName
   */
  def findByRefName(refName : String) = dao.groups.filter (_.refName === refName)

  /**
   * Find a list of groups that are linked to an application by using the application Id
   * @param applicationId The Id of the application which the groups belong to
   * @return The query to find the groups
   */
  def findByApplicationId(applicationId : Int) = dao.groups filter (_.applicationId === applicationId)

  /**
   * Find a list of groups that belong to an application by using the application refName
   * @param applicationRefName The refName of the application which the groups belong to
   * @return The query to find the groups
   */
  def findByApplicationRefName(applicationRefName : String) = for{
    group <- dao.groups
    app <- applicationDao.dao.applications if app.id === group.applicationId
  } yield group


}