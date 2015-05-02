package models.entity.role

import java.sql.Date

import play.api.db.slick.Profile


trait RoleComponent { this : Profile =>
  import profile.simple._

  class Roles(tag: Tag) extends Table[(Role)](tag, "ROLE") {

    def id: Column[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name : Column[String] = column[String] ("NAME",O.NotNull)
    def refName : Column[String] = column[String] ("REF_NAME",O.NotNull)
    def creationDate : Column[Date] = column[Date]("CREATION_DATE",O.NotNull)
    def description : Column[String] = column[String]("DESCRIPTION",O.Nullable)

    override def * = (id.?,name,refName,creationDate,description.?) <>(Role.tupled, Role.unapply)

  }

}

case class Role(id : Option[Int],name : String,refName : String,creationDate : Date,description : Option[String])
