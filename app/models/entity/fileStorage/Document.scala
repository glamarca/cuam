/*
 * Copyright (c) 2015. Gaëtan La Marca
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.entity.fileStorage

import java.sql.Date

import play.api.db.slick.Profile

trait DocumentComponent { this : Profile =>
  import profile.simple._

  class Documents(tag: Tag) extends Table[(Document)](tag, "DOCUMENT") {

    def id: Column[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name : Column[String] = column[String]("NAME",O.NotNull)
    def fileName : Column[String] = column[String]("FILE_NAME",O.NotNull)
    def mimeType : Column[String] = column[String]("MIME_TYPE",O.NotNull)
    def description : Column[String] = column[String]("DESCRIPTION",O.Nullable)
    def creationDate : Column[Date] = column[Date]("CREATION_DATE",O.NotNull)
    def updateDate : Column[Date] = column[Date]("MODIFICATION_DATE",O.NotNull)
    def updatingUser : Column[String] = column[String]("UPDATING_USER",O.NotNull)


    override def * = (id.?,name,fileName,mimeType,description.?,creationDate,updateDate,updatingUser) <>(Document.tupled,Document.unapply)

  }

}

case class Document(id : Option[Int],name : String,fileName : String,mimeType : String,description : Option[String],creationDate : Date,updateDate : Date,updatingUser : String)
