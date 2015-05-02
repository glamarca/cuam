package models.entity.group

import java.sql.Date

import play.api.db.slick.Profile


trait RoleGroupComponent { this : Profile =>
  import profile.simple._

  class RolesGroups(tag: Tag) extends Table[(RoleGroup)](tag, "ROLE_GROUP") {
    def id: Column[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def roleId : Column[Int] = column[Int]("ROLE_ID",O.NotNull)
    def groupId : Column[Int] = column[Int]("GROUP_ID",O.NotNull)
    def added : Column[Date] = column[Date]("ADDED",O.NotNull)

    override def * = (id.?,roleId,groupId,added) <>(RoleGroup.tupled, RoleGroup.unapply)

  }

}

case class RoleGroup(id : Option[Int],roleId : Int,groupId : Int,added : Date)
