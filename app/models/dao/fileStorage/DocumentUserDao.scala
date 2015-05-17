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

package models.dao.fileStorage

import models.entity.fileStorage.DocumentUserComponent
import play.api.db.slick.{Config, Profile}

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery
import play.api.db.slick.Config.driver.simple._

class DocumentUserDao(override val profile: JdbcProfile) extends DocumentUserComponent with Profile {
  val documentsUsers = TableQuery[DocumentsUsers]
}

object documentUserDao {
  val dao = new DocumentUserDao(Config.driver)

  def findDocumentByUserAndDocumentName(userId :Int ,documentName : String) = for {
      userDocument <- dao.documentsUsers if userDocument.userId === userId
      document <- documentDao.dao.documents if document.id === userDocument.documentId && document.name === documentName
  } yield document
}

