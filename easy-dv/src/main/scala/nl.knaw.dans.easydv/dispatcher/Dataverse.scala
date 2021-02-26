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
package nl.knaw.dans.easydv.dispatcher

import nl.knaw.dans.easydv.Command.FeedBackMessage
import nl.knaw.dans.easydv.CommandLineOptions
import nl.knaw.dans.lib.dataverse.DataverseApi
import nl.knaw.dans.lib.dataverse.model.DefaultRole
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats }

import java.io.PrintStream
import scala.util.{ Failure, Try }
import scala.language.reflectiveCalls

object Dataverse extends DebugEnhancedLogging {
  private implicit val jsonFormats: Formats = DefaultFormats

  def dispatch(commandLine: CommandLineOptions, dv: DataverseApi)(implicit resultOutput: PrintStream): Try[FeedBackMessage] = {
    trace(())
    commandLine.subcommands match {
      case commandLine.dataverse :: commandLine.dataverse.create :: Nil =>
        for {
          dvDef <- getStringFromStd
          _ = debug(dvDef)
          response <- dv.create(dvDef)
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "create"
      case commandLine.dataverse :: commandLine.dataverse.view :: Nil =>
        for {
          response <- dv.view()
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "view"
      case commandLine.dataverse :: commandLine.dataverse.delete :: Nil =>
        for {
          response <- dv.delete()
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "delete"
      case commandLine.dataverse :: commandLine.dataverse.contents :: Nil =>
        for {
          response <- dv.contents()
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "contents"
      case commandLine.dataverse :: commandLine.dataverse.storageSize :: Nil =>
        for {
          response <- dv.storageSize()
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "storage-size"
      case commandLine.dataverse :: commandLine.dataverse.listRoles :: Nil =>
        for {
          response <- dv.listRoles()
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "list-roles"
      case commandLine.dataverse :: commandLine.dataverse.listFacets :: Nil =>
        for {
          response <- dv.listFacets()
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "list-facets"
      case commandLine.dataverse :: (c @ commandLine.dataverse.setFacets) :: Nil =>
        for {
          response <- dv.setFacets(c.facets())
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "set-facets"
      case commandLine.dataverse :: commandLine.dataverse.createRole :: Nil =>
        for {
          dvDef <- getStringFromStd
          _ = debug(dvDef)
          response <- dv.createRole(dvDef)
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "create-role"
      case commandLine.dataverse :: commandLine.dataverse.listRoleAssignments :: Nil =>
        for {
          response <- dv.listRoleAssignments()
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "list-role-assignments"
      case commandLine.dataverse :: (c @ commandLine.dataverse.setDefaultRole) :: Nil =>
        for {
          defaultRole <- Try { DefaultRole.withName(c.role().toLowerCase) }
          response <- dv.setDefaultRole(defaultRole)
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "set-default-role"
      // TODO: assign-role
      // TODO: delete-role-assignment
      // TODO: list-metadata-blocks
      // TODO: set-metadata-blocks
      // TODO: is-metadata-blocks-root
      // TODO: set-metadata-blocks-root
      // TODO: create-dataset
      // TODO: import-dataset
      // TODO: import-dataset-ddi
      // TODO: publish

      case _ => Failure(new RuntimeException(s"Unkown dataverse sub-command: ${ commandLine.args.tail.mkString(" ") }"))
    }
  }
}
