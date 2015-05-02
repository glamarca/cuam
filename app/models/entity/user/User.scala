package models.entity.user

import java.sql.Date

import play.api.db.slick.Profile

trait UserComponent { this : Profile =>
  import profile.simple._

  class Users(tag: Tag) extends Table[(User)](tag, "USER") {

    def id: Column[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def userName : Column[String] = column[String]("USER_NAME",O.NotNull)
    def password : Column[String] = column[String]("PASSWORD",O.NotNull)
    def lastname : Column[String] = column[String]("LASTNAME",O.Nullable)
    def firstName : Column[String] = column[String]("FIRSTNAME",O.Nullable)
    def email : Column[String] = column[String]("EMMAIL",O.NotNull)
    def creationDate : Column[Date] = column[Date]("CREATION_DATE",O.NotNull)
    def updateDate : Column[Date] = column[Date]("MODIFICATION_DATE",O.NotNull)
    def updatingUser : Column[String] = column[String]("UPDATING_USER",O.NotNull)

    override def * = (id.?,userName,password,lastname.?,firstName.?,email,creationDate,updateDate,updatingUser) <>(User.tupled, User.unapply)

  }

}

case class User(id : Option[Int],userName : String,password : String,lastName : Option[String],firstName : Option[String],email : String,creationDate : Date,updateDate : Date,updatingUser : String)
