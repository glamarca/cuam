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

