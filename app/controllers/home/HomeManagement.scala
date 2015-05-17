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

package controllers.home

import controllers.home
import controllers.security.Authentication._
import controllers.security.Secured
import models.dao.user.userDao
import models.entity.user.User
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.i18n.Messages
import play.api.mvc.{Security, Controller}
import org.mindrot.jbcrypt.BCrypt

object HomeManagement extends Controller with Secured {

  def index(message : Option[String]) = withAuth { username => implicit request =>
    Ok(views.html.index(message))
  }

  def indexWithUserExistsTest = DBAction { implicit request => {
    if (userDao.dao.users.list.isEmpty) {
      val date = new java.sql.Date(new java.util.Date().getTime())
      userDao.dao.users += User(None, "admin", BCrypt.hashpw("admin", BCrypt.gensalt), Some("admin"), Some("admin"), "admin@cuam.org", date, date, "init")
      Redirect(home.routes.HomeManagement.index(Some(Messages("tempAdminWarning")))).withSession(Security.username -> "admin")
    }
    else {
      Redirect(routes.HomeManagement.index(None)).withNewSession
    }
  }

  }
}
