/*
Copyright 2015 La Marca Gaëtan

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
package models.entity.group

import java.sql.Date

import play.api.db.slick.Profile


trait GroupComponent { this : Profile =>
  import profile.simple._

  class Groups(tag: Tag) extends Table[(Group)](tag, "GROUPS") {

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
