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

import models.entity.fileStorage.DocumentComponent
import play.api.db.slick.{Config, Profile}

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery
import play.api.db.slick.Config.driver.simple._


class DocumentDao(override val profile: JdbcProfile) extends DocumentComponent with Profile {
  val documents = TableQuery[Documents]
}

object documentDao {
  val dao = new DocumentDao(Config.driver)

  /**
   * Find a document by name and fileName
   * @param name The name of the doument
   * @param fileName The filename of the stored document
   * @return The query to find the document
   */
  def findByNameAndFileName(name : String,fileName : String) = dao.documents filter (d => d.name === name && d.fileName === fileName)
}

