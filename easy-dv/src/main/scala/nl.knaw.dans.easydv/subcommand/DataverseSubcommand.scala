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

class DataverseSubcommand extends AbstractSubcommand("dataverse") {
  shortSubcommandsHelp(true)
  descr("Operations on a dataverse or sub-dataverse. See: https://guides.dataverse.org/en/latest/api/native-api.html#dataverses")
  val alias: ScallopOption[String] = trailArg("dataverse-alias",
    descr = "The dataverse alias")

  val create = addSimpleCommand(
    name = "create",
    description = "Creates a dataverse based on the JSON definition on STDIN. See: https://guides.dataverse.org/en/latest/api/native-api.html#create-a-dataverse")
  val view = addSimpleCommand(
    name = "view",
    description = "Displays metadata about a dataverse. See: https://guides.dataverse.org/en/latest/api/native-api.html#view-a-dataverse")
  val delete = addSimpleCommand(
    name = "delete",
    description = "Deletes a dataverse. See: https://guides.dataverse.org/en/latest/api/native-api.html#delete-a-dataverse")
  val contents = addSimpleCommand(
    name = "contents",
    description = "Displays dataverse contents. See: https://guides.dataverse.org/en/latest/api/native-api.html#show-contents-of-a-dataverse")

  // TODO: storage-size
  val storageSize = addSimpleCommand(
    name = "storage-size",
    description = "Displays the storages size in bytes for the dataverse. See: https://guides.dataverse.org/en/latest/api/native-api.html#report-the-data-file-size-of-a-dataverse")

  val listRoles = addSimpleCommand(
    name = "list-roles",
    description = "Lists roles defined in dataverse. See: https://guides.dataverse.org/en/latest/api/native-api.html#list-roles-defined-in-a-dataverse")

  // TODO: list-facets
  val listFacets = addSimpleCommand(
    name = "list-facets",
    description = "Lists facets for the dataverse. See: https://guides.dataverse.org/en/latest/api/native-api.html#list-facets-configured-for-a-dataverse")

  // TODO: set-facets

  val createRole = addSimpleCommand(
    name = "create-role",
    description = "Creates a role based on the JSON definition on STDIN. See: https://guides.dataverse.org/en/latest/api/native-api.html#create-a-new-role-in-a-dataverse")

  // TODO: list-role-assignments
  val listRoleAssignments = addSimpleCommand(
    name = "list-role-assignments",
    description = "List all the role assignments at the given dataverse. See: https://guides.dataverse.org/en/latest/api/native-api.html#list-role-assignments-in-a-dataverse")

  // TODO: set-default-role
  val setDefaultRole = addSimpleCommand(
    name = "set-default-role",
    description = "Assigns a default role to a user creating a dataset in a dataverse. See: https://guides.dataverse.org/en/latest/api/native-api.html#assign-default-role-to-user-creating-a-dataset-in-a-dataverse")

  // TODO: assign-role
  val assignRole = addSimpleCommand(
    name = "assign-role",
    description = "Assigns a role. See: https://guides.dataverse.org/en/latest/api/native-api.html#assign-a-new-role-on-a-dataverse")

  // TODO: delete-role-assignment
  val deleteRoleAssignment = addSimpleCommand(
    name = "delete-role-assignment",
    description = "Deletes a role assignment. Use list-role-assignments to get the ID")

  // TODO: list-metadata-blocks
  // TODO: set-metadata-blocks
  // TODO: is-metadata-blocks-root
  // TODO: set-metadata-blocks-root
  // TODO: create-dataset
  // TODO: import-dataset
  // TODO: import-dataset-ddi
  // TODO: publish

  footer(subCommandFooter)
}
