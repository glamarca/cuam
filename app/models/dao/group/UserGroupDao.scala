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

import models.dao.user.userDao
import models.entity.group.UserGroupComponent
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{Config, Profile}

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery

class UserGroupDao(override val profile: JdbcProfile) extends UserGroupComponent with Profile {

  val usersgroups = TableQuery[UsersGroups]
}

object userGroupDao {
  val dao = new UserGroupDao(Config.driver)

  /**
   * Find the users that belong to a group
   * @param groupId The group Id
   * @return The query to find the users.
   */
  def findUsers(groupId : Int) = for {
    userGroup <- dao.usersgroups if userGroup.groupId === groupId
    user <- userDao.dao.users if user.id === userGroup.userId
  } yield user

  /**
   * Find the groups in which the user is
   * @param userId The user Id
   * @return The query to find the groups.
   */
  def findGroups(userId : Int) = for {
    userGroup <- dao.usersgroups if userGroup.userId === userId
    group <- groupDao.dao.groups if group.id === userGroup.groupId
  } yield group

}