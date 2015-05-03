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

import models.dao.permission.permissionDao
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

  /**
   * Find the permissions from a group
   * @param groupId The group Id
   * @return The query that find the permissions list
   */
  def findPermissions(groupId : Int) = for {
    permissionGroup <- dao.permissionsGroups if permissionGroup.groupId === groupId
    permission <- permissionDao.dao.permissions if permission.id === permissionGroup.permissionId
  } yield permission

  /**
   * Find the groups where the permission is
   * @param permissionId The permision Id
   * @return The query that find the group list
   */
  def findGroups(permissionId : Int) = for {
    permissionGroup <- dao.permissionsGroups if permissionGroup.permissionId === permissionId
    group <- groupDao.dao.groups if group.id === permissionGroup.groupId
  } yield group
}