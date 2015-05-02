package models.dao.role

import models.entity.role.RoleComponent
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{Config, Profile}

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery

class RoleDao(override val profile: JdbcProfile) extends RoleComponent with Profile {
  val roles = TableQuery[Roles]
}

object roleDao {

  val dao = new RoleDao(Config.driver)

  def search(name : Column[Option[String]],refName : Column[Option[String]]) =
    dao.roles filter { p =>
      Case.If(name.isDefined).Then(p.name === name).Else(Some(true)) &&
      Case.If(refName.isDefined).Then(p.refName === refName).Else(Some(true))
  }

  def searchByNames(name : Option[String],refName : Option[String]) = search(name,refName)

  def searchById(id : Int) = dao.roles filter (p => p.id === id)

  def searchByRefName(refName: String) = dao.roles filter (p => p.refName === refName)


}
