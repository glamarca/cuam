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

/**
 * @author
 */
package controllers.user

case class UserForm(userId: Option[Int], userName: Option[String], lastName: Option[String], firstName: Option[String], password: Option[String], email: Option[String],userPicture : Option[String])

case class PasswordForm(userId : Int,oldPassword : Option[String],newPassword : Option[String])
