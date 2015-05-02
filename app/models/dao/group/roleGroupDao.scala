package models.dao.group

import models.entity.group.RoleGroupComponent
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{Config, Profile}

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery

class RoleGroupDao(override val profile: JdbcProfile) extends RoleGroupComponent with Profile {
  val rolesGroups = TableQuery[RolesGroups]
}

object roleGroupDao {

  val dao = new RoleGroupDao(Config.driver)

  def search(roleId: Column[Option[Int]], groupId: Column[Option[Int]]) =
    dao.rolesGroups filter { p =>
      Case.If(roleId.isDefined).Then(p.roleId === roleId).Else(Some(true)) &&
        Case.If(groupId.isDefined).Then(p.groupId === groupId).Else(Some(true))
    }

  def searchByRoleGroup(roleId: Option[Int], groupId: Option[Int]) = search(roleId, groupId)

}