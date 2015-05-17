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

package util

import java.security.MessageDigest
import models.references._
import play.Play._
import play.api.i18n.Messages


object fileStorage {

  def getHashedName(name : String) = MessageDigest.getInstance("SHA").digest(name.getBytes).map("%02X".format(_)).mkString

  def MimeTypefromRefName(refName : String) : MimeType = refName match {
    case "PDF" => PDF()
    case "XML" => XML()
    case "JSON" => JSON()
    case "PNG" => PNG()
    case _ => TEXT()
  }

  def getStorageFolderPathByDocumentName(documentName : String) : Option[String] = documentName match {
    case path if Messages("document.qrCode") == path => Some(application.configuration.getString("qrCodesDirPath"))
    case path if Messages("document.userPicture") == path => Some(application.configuration.getString("usersPictureDirPath"))
    case _ => None
  }
}
