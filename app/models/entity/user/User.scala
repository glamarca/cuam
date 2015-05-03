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

/**
 * @author
 */
package models.entity.user

import java.sql.Date

import play.api.db.slick.Profile

trait UserComponent { this : Profile =>
  import profile.simple._

  class Users(tag: Tag) extends Table[(User)](tag, "USER") {

    def id: Column[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def userName : Column[String] = column[String]("USER_NAME",O.NotNull)
    def password : Column[String] = column[String]("PASSWORD",O.NotNull)
    def lastName : Column[String] = column[String]("LASTNAME")
    def firstName : Column[String] = column[String]("FIRSTNAME")
    def email : Column[String] = column[String]("EMAIL",O.NotNull)
    def creationDate : Column[Date] = column[Date]("CREATION_DATE",O.NotNull)
    def updateDate : Column[Date] = column[Date]("MODIFICATION_DATE",O.NotNull)
    def updatingUser : Column[String] = column[String]("UPDATING_USER",O.NotNull)

    override def * = (id.?,userName,password,lastName.?,firstName.?,email,creationDate,updateDate,updatingUser) <>(User.tupled, User.unapply)

  }

}

case class User(id : Option[Int],userName : String,password : String,lastName : Option[String],firstName : Option[String],email : String,creationDate : Date,updateDate : Date,updatingUser : String)
