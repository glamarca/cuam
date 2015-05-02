package controllers.user

import models.dao.user.userDao
import models.entity.user.User
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller}
import views.html.user._

object UserManagement extends Controller{

  val userForm = Form(
    mapping(
      "userId" -> default(optional(number),None),
      "userName" -> default(optional(text),None),
      "lastName" -> default(optional(text),None),
      "firstName" -> default(optional(text),None),
      "password" -> default(optional(text),None),
      "email" -> default(optional(email),None)
    )(UserForm.apply)(UserForm.unapply)
  )

  def userIndex = Action { implicit request =>
    Ok(userManagementView(userForm,None))
  }

  def showUser(id : Int) = DBAction { implicit request =>
    val user = (userDao.searchById(id)).list.head
    val userProfileForm = UserForm(user.id,Some(user.userName),user.lastName,user.firstName,Some(user.password),Some(user.email))
    Ok(userView(userProfileForm))
  }

  def showUserForm(id : Option[Int]) = DBAction{ implicit request =>
    if(id.isDefined){
      val user = (userDao.searchById(id.get)).list.head
      val form = userForm fill (UserForm(user.id,Some(user.userName),user.lastName,user.firstName,Some(user.password),Some(user.email)))
      Ok(UserFormView(form)(Messages("userUpdate")))
    }
    else{
      Ok(UserFormView(userForm)(Messages("userCreation")))
    }
  }

  def addUser(form: UserForm) : UserForm = DB.withSession { implicit request =>
          val date = new java.sql.Date(new java.util.Date().getTime())
          userDao.dao.users += User(None,form.userName.get,form.password.get,form.lastName,form.firstName,form.email.get,date,date,"user")
          val user = (userDao.serachByUserNameOrEmailAndPassword(form.userName,form.email,form.password.get)).list.head
          UserForm(user.id,Some(user.userName),user.lastName,user.firstName,Some(user.password),Some(user.email))
    }

  def updateUser(form: UserForm) : UserForm = DB.withSession { implicit request =>
    val user = (userDao.searchById(form.userId.get)).list.head
    val userWithDate = User(user.id,user.userName,user.password,user.lastName,user.firstName,user.email,user.creationDate,new java.sql.Date(new java.util.Date().getTime()),"user")
    userDao.dao.users.filter(_.id === form.userId).update(userWithDate)
    val updatedUser = (userDao.searchById(form.userId.get)).list.head
    UserForm(updatedUser.id,Some(updatedUser.userName),updatedUser.lastName,updatedUser.firstName,Some(updatedUser.password),Some(updatedUser.email))
  }

  def updateCreateUser = Action { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(UserFormView(formWithErrors)(Messages("userUpdate")))
      },
      form =>{
        if(form.userId.isDefined){
          val returnedForm = updateUser(form)
          Ok(userView(returnedForm))
        }
        else{
          val returnedForm = addUser(form)
          Ok(userView(returnedForm))
        }
      }
    )
  }

  def findUser = DBAction { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(userManagementView(formWithErrors,None))
      },
      searchForm => {
        val usersList = userDao.searchByForm(searchForm).list
        Ok(userManagementView(userForm,Some(usersList)))
      }
    )
  }

  def deleteUser(id : Int) = DBAction {implicit request =>
    userDao.dao.users.filter(_.id === id).mutate(_.delete)
    Ok(userManagementView(userForm,None))
  }

}
