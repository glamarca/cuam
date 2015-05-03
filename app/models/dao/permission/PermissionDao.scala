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
package models.dao.permission

import java.util.Locale

import models.entity.permission.PermissionComponent
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{Config, Profile}

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery

class PermissionDao(override val profile: JdbcProfile) extends PermissionComponent with Profile {
  val permissions = TableQuery[Permissions]
}

object permissionDao {

  val dao = new PermissionDao(Config.driver)

  def search(name : Column[Option[String]],refName : Column[Option[String]]) =
    dao.permissions filter { p =>
      Case.If(name.isDefined).Then(p.name.toUpperCase === name.toUpperCase).Else(Some(true)) &&
      Case.If(refName.isDefined).Then(p.refName === refName.toUpperCase).Else(Some(true))
  }

  def findByNames(name : Option[String],refName : Option[String]) = search(name,refName)

  def findById(id : Int) = dao.permissions filter (p => p.id === id)

  def findByRefName(refName: String) = dao.permissions filter (p => p.refName === refName.toUpperCase)

  def findByNameOrRefName(name : String , refName : String) = dao.permissions filter (p => p.name.toUpperCase === name.toUpperCase || p.refName === refName.toUpperCase)


}
