/**
 * Copyright (C) 2021 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easydv.subcommand

import nl.knaw.dans.easydv.subCommandFooter
import org.rogach.scallop.{ ScallopOption, Subcommand }

class DataverseSubcommand extends Subcommand("dataverse") {
  shortSubcommandsHelp(true)
  descr("Operations on a dataverse or sub-dataverse")
  val alias: ScallopOption[String] = trailArg("dataverse-alias",
    descr = "The dataverse alias")

  val create = new Subcommand("create") {
    descr("Creates a dataverse based on the JSON definition on STDIN. See: https://guides.dataverse.org/en/latest/api/native-api.html#create-a-dataverse")
  }
  addSubcommand(create)

  val view = new Subcommand("view") {
    descr("View metadata about a dataverse. See: https://guides.dataverse.org/en/latest/api/native-api.html#view-a-dataverse")
  }
  addSubcommand(view)

  footer(subCommandFooter)
}
