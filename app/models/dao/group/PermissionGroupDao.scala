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
package models.dao.group

import models.entity.group.PermissionGroupComponent
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{Config, Profile}

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery

class PermissionGroupDao(override val profile: JdbcProfile) extends PermissionGroupComponent with Profile {
  val permissionsGroups = TableQuery[PermissionsGroups]
}

object permissionGroupDao {

  val dao = new PermissionGroupDao(Config.driver)

  def search(permissionId: Column[Option[Int]], groupId: Column[Option[Int]]) =
    dao.permissionsGroups filter { p =>
      Case.If(permissionId.isDefined).Then(p.permissionId === permissionId).Else(Some(true)) &&
        Case.If(groupId.isDefined).Then(p.groupId === groupId).Else(Some(true))
    }

  def searchByPermissionGroup(permissionId: Option[Int], groupId: Option[Int]) = search(permissionId, groupId)

}