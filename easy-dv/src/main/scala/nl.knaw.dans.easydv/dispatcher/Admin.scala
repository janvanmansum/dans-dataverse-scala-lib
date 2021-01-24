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
import nl.knaw.dans.lib.dataverse.AdminApi
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats }

import java.io.PrintStream
import scala.language.reflectiveCalls
import scala.util.{ Failure, Try }

object Admin extends DebugEnhancedLogging {
  private implicit val jsonFormats: Formats = DefaultFormats

  def dispatch(commandLine: CommandLineOptions, admin: AdminApi)(implicit resultOutput: PrintStream): Try[FeedBackMessage] = {
    trace(())
    commandLine.subcommands match {
      case commandLine.admin :: commandLine.admin.getAllWorkflows :: Nil =>
        for {
          response <- admin.getWorkflows
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "get-all-workflows"
      case commandLine.admin :: commandLine.admin.getWorkflow :: Nil =>
        for {
          response <- admin.getWorkflow(commandLine.admin.getWorkflow.id())
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "get-workflow"
      case commandLine.admin :: commandLine.admin.addWorkflow :: Nil =>
        for {
          s <- getStringFromStd
          response <- admin.addWorkflow(s)
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "add-workflow"
      case commandLine.admin :: commandLine.admin.deleteWorkflow :: Nil =>
        for {
          response <- admin.deleteWorkflow(commandLine.admin.deleteWorkflow.id())
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "delete-workflow"
      case commandLine.admin :: commandLine.admin.getAllDefaultWorkflows :: Nil =>
        for {
          response <- admin.getDefaultWorkflows
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "get-all-default-workflows"

      case _ => Failure(new RuntimeException(s"Unkown command: $commandLine"))
    }
  }
}
