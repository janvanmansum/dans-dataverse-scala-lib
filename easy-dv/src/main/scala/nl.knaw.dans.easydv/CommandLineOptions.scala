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
package nl.knaw.dans.easydv

import nl.knaw.dans.easydv.subcommand._
import org.rogach.scallop.ScallopConf

class CommandLineOptions(args: Array[String], configuration: Configuration) extends ScallopConf(args) {
  appendDefaultToDescription = true
  editBuilder(_.setHelpWidth(200))
  printedName = "easy-dv"
  version(configuration.version)
  val description: String = s"""Easy-to-use command line client for the Dataverse API"""
  val synopsis: String =
    s"""
       |  $printedName dataverse <alias> <sub-command>
       |  $printedName dataset <id> <sub-command>
       |  $printedName file <id> <sub-command>
       |  $printedName admin <sub-command>
       |  $printedName sword <sub-command>
       |  $printedName create-builtin-user
       |  $printedName resume-workflow
     """.stripMargin

  version(s"$printedName v${ configuration.version }")
  banner(
    s"""
       |  $description
       |
       |Usage:
       |
       |$synopsis
       |
       |Options:
       |""".stripMargin)

  val dataverse = new DataverseSubcommand()
  addSubcommand(dataverse)
  val dataset = new DatasetSubcommand()
  addSubcommand(dataset)
  val file = new FileSubcommand()
  addSubcommand(file)
  val admin = new AdminSubcommand()
  addSubcommand(admin)
  val sword = new SwordSubcommand()
  addSubcommand(sword)

  // TODO: Add 'stray' commands create-builtin-user and resume-workflow

}
