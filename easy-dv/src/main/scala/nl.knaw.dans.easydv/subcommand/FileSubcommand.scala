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
import org.rogach.scallop.ScallopOption

class FileSubcommand extends AbstractSubcommand("file") {
  shortSubcommandsHelp(true)
  descr("Operations on a file. See: https://guides.dataverse.org/en/latest/api/native-api.html#files")
  val id: ScallopOption[String] = trailArg("id",
    descr = "dataset identifier; if it consists of only numbers, it is taken to be a database ID, otherwise as a persistent ID")

  val updateMetadata = addSimpleCommand(
    name = "update-metadata",
    description = "Updates the file metadata. See: https://guides.dataverse.org/en/latest/api/native-api.html#updating-file-metadata")

  footer(subCommandFooter)
}
