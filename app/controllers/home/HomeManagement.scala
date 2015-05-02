package controllers.home

import play.api.mvc.{Action, Controller}

/**
 * Created by sarace on 25/04/15.
 */
object HomeManagement extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

}
