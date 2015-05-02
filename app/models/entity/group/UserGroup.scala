package models.entity.group

import java.sql.Date

import play.api.db.slick.Profile


trait UserGroupComponent { this : Profile =>
  import profile.simple._

  class UsersGroups(tag: Tag) extends Table[(UserGroup)](tag, "USER_GROUP") {

    def id: Column[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def userId : Column[Int] = column[Int]("USER_ID",O.NotNull)
    def groupId : Column[Int] = column[Int]("GROUP_ID",O.NotNull)
    def added : Column[Date] = column[Date]("ADDED",O.NotNull)

    override def * = (id.?,userId,groupId,added) <>(UserGroup.tupled, UserGroup.unapply)

  }

}

case class UserGroup(id : Option[Int],userId : Int,groupId : Int,added : Date)

