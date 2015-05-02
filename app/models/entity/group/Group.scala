package models.entity.group

import java.sql.Date

import play.api.db.slick.Profile


trait GroupComponent { this : Profile =>
  import profile.simple._

  class Groups(tag: Tag) extends Table[(Group)](tag, "GROUP") {

    def id: Column[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name : Column[String] = column[String]("NAME",O.NotNull)
    def refName : Column[String] = column[String]("REF_NAME",O.NotNull)
    def creationDate : Column[Date] = column[Date]("CREATION_DATE",O.NotNull)
    def updateDate : Column[Date] = column[Date]("MODIFICATIO_DATE",O.NotNull)
    def updatingUser : Column[String] = column[String]("UPDATING_USER",O.NotNull)
    def descritpion : Column[String] = column[String]("DESCRIPTION")

    override def * = (id.?,name,refName,creationDate,updateDate,updatingUser.?,descritpion.?) <>(Group.tupled, Group.unapply)

  }

}

case class Group(id : Option[Int],name : String,refName : String,creationDate : Date,updateDate : Date,updatingUser : Option[String],descritpion : Option[String])
