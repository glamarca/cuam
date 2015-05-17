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

package controllers.security

import models.dao.user.userDao
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller}
import controllers.{home,security}
import play.api.mvc.Security
import org.mindrot.jbcrypt.BCrypt

object Authentication extends Controller {

  val loginForm = Form(
    tuple(
      "unameemail" -> text,
      "password" -> text
    ) verifying (Messages("invalidUnameEmailOrPassword"), result => result match {
      case (unameemail, password) => check(unameemail, password)
    })
  )

  def check(unameemail: String, password: String) = DB.withSession {implicit request =>
    val userList = userDao.findByUserNameOrEmail(unameemail,unameemail).list
    !userList.isEmpty && BCrypt.checkpw(password,userList(0).password)
   }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(home.routes.HomeManagement.index(None)).withSession(Security.username -> user._1)
    )
  }

  def logout = Action {
    Redirect(security.routes.Authentication.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }

}
