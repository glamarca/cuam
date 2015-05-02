package controllers.role

import models.dao.role.roleDao
import models.entity.role.Role
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller}
import views.html.role._
object RoleManagement extends Controller {

  /**
   * Le formulaire de creation et modification d'une role
   */
  val roleForm = Form(
    mapping(
      "id" -> default(optional(number), None),
      "name" -> nonEmptyText,
      "refName" -> nonEmptyText,
      "creationDate" -> default(sqlDate,null),
      "description" -> default(optional(text), None)
    )(Role.apply)(Role.unapply)
  )

  /**
   * Le formualire de recherche d'une role
   */
  val searchForm = Form(
    tuple(
      "refName" -> default(optional(text),None),
      "name" -> default(optional(text),None)
    )
  )

  /**
   * Page d'accueil de la gestion des roles
   * @return Une liste de roles vide dans la vue recherche
   */
  def roleIndex = Action { implicit request =>
    Ok(roleManagementView(None))
  }

  /**
   * Recherche une liste de role en fonction de "fomulaireRecherche"
   * @return La Liste des role trouvées , dans la vue de recherche
   */
  def findRole = DBAction { implicit request =>
    searchForm.bindFromRequest.fold(
      formWithError =>{
        BadRequest(roleManagementView(None))
      },
      searchForm =>{
        val rolesList = roleDao.searchByNames(searchForm._1,searchForm._2).list
        Ok(roleManagementView(Some(rolesList)))
      }
    )
  }

  /**
   * Permet d'afficher la fiche d'une role
   * @param id L'id de la role
   * @return La vue de la fiche de role
   */
  def showRole(id : Int) = DBAction { implicit request =>
      val role = roleDao.searchById(id).first
      Ok(roleView(role,None))
  }

  /**
   * Mise à jour de la roled ans la DB et renvoie de la nouvelle role
   * @param role La role à mettre à jour
   * @return La role mise à jour.
   */
  def updateRole(role: Role): Role = DB.withSession { implicit request =>
    roleDao.searchById(role.id.get).map(p => (p.name,p.refName,p.description)).update((role.name,role.refName,role.description.orNull))
    roleDao.searchById(role.id.get).first
  }

  /**
   * Création d'une role dans la DB
   * @param role La role à créer
   * @return La fiche de la role créée
   */
  def createRole(role: Role) : Role = DB.withSession{implicit request =>
    val roleWithDate = Role(None,role.name,role.refName,new java.sql.Date(new java.util.Date().getTime()),role.description)
    roleDao.dao.roles += roleWithDate
    roleDao.searchByRefName(role.refName).first
  }

  /**
   * Permet de lancer la creation ou la modification d'une role en fonction de "roleForm"
   * @return La fiche de la role crée
   */
  def updateCreateRole = DBAction { implicit request =>
    roleForm.bindFromRequest.fold(
      formWithError => {
        BadRequest(roleFormView(formWithError)(Messages("erreurFormulaire")))
      },
      formulaire => {
        if(formulaire.id.isDefined){
          val updatedRole = updateRole(formulaire)
          Redirect(routes.RoleManagement.showRole(updatedRole.id.get))
        }
        else{
          if(!roleDao.searchByRefName(formulaire.refName).list.isEmpty) {
            BadRequest(roleFormView(roleForm.fill(formulaire).withError("refName",Messages("roleExiste")))(Messages("creationrole")))
          }
          else{
            val updatedRole = createRole(formulaire)
            Redirect(routes.RoleManagement.showRole(updatedRole.id.get))
          }
        }
      }
    )
  }

  /**
   * Supprime une role
   * @param id L'id de la role à supprimer
   * @return La vue de recherche des roles
   */
  def deleteRole(id : Int) = DBAction {implicit request =>
    roleDao.searchById(id).mutate(_.delete)
    Ok(roleManagementView(None))
  }

  /**
   * Affiche une fiche de role
   * @param id L'id de la role à afficher
   * @return La vue de la fiche de role
   */
  def showRoleForm(id : Option[Int]) = DBAction { implicit request =>
    if(id.isDefined){
      val role = roleDao.searchById(id.get).first
      Ok(roleFormView(roleForm.fill(role))(Messages("roleUpdate")))
    }
    else{
      Ok(roleFormView(roleForm)(Messages("roleCreation")))
    }
  }

}
