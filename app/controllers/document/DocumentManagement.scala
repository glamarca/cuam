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

package controllers.document

import java.nio.file.{Paths, Files}

import models.dao.fileStorage.{documentDao, documentUserDao}
import models.entity.fileStorage.{DocumentUser, Document}
import models.references.PNG
import play.Play._
import play.api.db.slick._
import play.api.i18n.Messages
import play.api.mvc.Controller
import util.fileStorage
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import org.mindrot.jbcrypt.BCrypt
import net.glxn.qrgen._

object DocumentManagement extends Controller{


  def getUserDocument(userId : Option[Int],documentName : String) = DBAction{implicit request =>
    if(userId.isDefined){
      val doc = documentUserDao.findDocumentByUserAndDocumentName(userId.get,documentName).firstOption
      if(doc.isDefined){
        val mimeType = fileStorage.MimeTypefromRefName(doc.get.mimeType)
        val storagePath = s"${fileStorage.getStorageFolderPathByDocumentName(doc.get.name).getOrElse("")}/${doc.get.fileName}${mimeType.extention}"
        try {
          val documentData: Array[Byte] = Files.readAllBytes(Paths.get(storagePath))
          Ok(documentData).as(mimeType.mimeType)
        }
        catch {
          case e: IllegalArgumentException =>
            BadRequest("Couldn’t generate document. Error: " + e.getMessage)
        }
      }
      else{
        Ok(Array[Byte]())
      }
    }
    else{
      Ok(Array[Byte]())
    }
  }

  def createUserPicture(userId : Int,userPictureName : Option[String]) = DB.withSession {implicit request =>
    val today = new java.sql.Date(new java.util.Date().getTime)
    val pictureDoc = userPictureName match {
      case None => Document(None,Messages("document.userPicture"),Messages("defaultUserPicture"),PNG().refName,None,today,today,"user")
      case Some(fileName) => {
        if(!fileName.isEmpty) {
          val doc = Document(None, Messages("document.userPicture"), fileName, PNG().refName, None, today, today, "user")
          val storagePath = s"${fileStorage.getStorageFolderPathByDocumentName(doc.name).getOrElse("")}/${doc.fileName}${PNG().extention}"
          Files.copy(Paths.get(s"/tmp/picture/$fileName"), Paths.get(storagePath))
          doc
        }
        else{
          Document(None,Messages("document.userPicture"),Messages("defaultUserPicture"),PNG().refName,None,today,today,"user")
        }
      }
    }
    documentDao.dao.documents += pictureDoc
    val updatedDocument = documentDao.findByNameAndFileName(pictureDoc.name,pictureDoc.fileName).first
    val userDocument = DocumentUser(None,userId,updatedDocument.id.get)
    documentUserDao.dao.documentsUsers += userDocument
  }

  def updatePicture(document: Document, pictureName: Option[String]) = if (pictureName.isDefined) DB.withSession {implicit request =>
    val today = new java.sql.Date(new java.util.Date().getTime)
    documentDao.dao.documents filter (_.id === document.id) map (d => (d.fileName,d.updateDate)) update ((document.fileName,today))
  }

  /**
   * Create the qrCode for User using his userName
   * @param userId The id of the user we want to add qrCode
   * @param userName The user username will be encrioted and included in qrCode
   * @return The Document representing the qrCode
   */
  def createUserQrCode(userId: Int,userName : String) : Document = DB.withSession {implicit request =>
    val today = new java.sql.Date(new java.util.Date().getTime)
    val qrCode = Document(None,Messages("document.qrCode"),fileStorage.getHashedName(userName),PNG().refName,Some(Messages("document.qrCodeDescription")),today,today,"user")
    documentDao.dao.documents += qrCode
    val updatedQrCode = documentDao.findByNameAndFileName(qrCode.name,qrCode.fileName).first
    val documentUser = DocumentUser(None,userId,updatedQrCode.id.get)
    documentUserDao.dao.documentsUsers += documentUser
    val qrCodeDataPath = s"${application.configuration.getString("qrCodesDirPath")}/${updatedQrCode.fileName}.png"
    Files.copy(QRCode.from(userName).file.toPath,Paths.get(qrCodeDataPath))
    updatedQrCode
  }
}
