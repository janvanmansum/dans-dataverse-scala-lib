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

class AdminSubcommand extends AbstractSubcommand("admin") {
  shortSubcommandsHelp(true)
  descr("Operations via de admin API. See: https://guides.dataverse.org/en/latest/api/native-api.html#admin")

  val getAllWorkflows = new Subcommand("get-all-workflows") {
    descr("Retrieves all the registered workflows")
  }
  addSubcommand(getAllWorkflows)

  val getWorkflow = new Subcommand("get-workflow") {
    descr("Retrieves the specified registered workflow")
    val id: ScallopOption[Int] = trailArg(name = "workflow-id", descr = "id of the workflow to retrieve", required = true)
  }
  addSubcommand(getWorkflow)

  // TODO: first add API
  val addWorkflow = new Subcommand("add-workflow") {
    descr("Adds the workflow as defined by the JSON definition on STDIN")
  }
  addSubcommand(addWorkflow)

  val deleteWorkflow = new Subcommand("delete-workflow") {
    descr("Deletes the specified workflow")
    val id: ScallopOption[Int] = trailArg(name = "workflow-id", descr = "id of the workflow to delete", required = true)
  }
  addSubcommand(deleteWorkflow)

  val getAllDefaultWorkflows = new Subcommand("get-all-default-workflows") {
    descr("Retrieves the default workflows for all trigger types")
  }
  addSubcommand(getAllDefaultWorkflows)

  val getDefaultWorkflow = new Subcommand("get-default-workflow") {
    descr("Retrieves the default workflow for the specified trigger type")
    val triggerType: ScallopOption[String] = opt(name = "trigger-type", descr = "the trigger type", required = true)
  }
  addSubcommand(getDefaultWorkflow)

  val setDefaultWorkflow = new Subcommand("set-default-workflow") {
    descr("Sets the default workflow for the specified trigger type")
    val triggerType: ScallopOption[String] = opt(name = "trigger-type", descr = "the trigger type", required = true)
    val id: ScallopOption[Int] = trailArg(name = "workflow-id", descr = "id of the workflow to set as default")
  }
  addSubcommand(setDefaultWorkflow)

  val unsetDefaultWorkflow = new Subcommand("unset-default-workflow") {
    descr("Unsets the default workflow for the specified trigger type")
    val triggerType: ScallopOption[String] = opt(name = "trigger-type", descr = "the trigger type", required = true)
  }
  addSubcommand(unsetDefaultWorkflow)

  // TODO: the 'whitelist' commands

  footer(subCommandFooter)
}
