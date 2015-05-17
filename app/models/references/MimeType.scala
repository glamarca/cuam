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

package models.references


sealed abstract class MimeType(val refName : String,val extention : String,val mimeType : String)

case class TEXT() extends MimeType("TEXT",".txt","text/plain")
case class PDF() extends  MimeType("PDF",".pdf","application/pdf")
case class XML() extends MimeType("XML",".xml","application/xml")
case class JSON() extends MimeType("JSON",".json","application/json")
case class PNG() extends MimeType("PNG",".png","image/png")
