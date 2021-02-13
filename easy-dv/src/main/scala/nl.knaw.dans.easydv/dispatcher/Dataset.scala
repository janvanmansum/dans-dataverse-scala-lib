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
import nl.knaw.dans.lib.dataverse.DatasetApi
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.lang.StringUtils
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats }

import java.io.PrintStream
import scala.language.reflectiveCalls
import scala.util.{ Failure, Success, Try }

object Dataset extends DebugEnhancedLogging {
  private implicit val jsonFormats: Formats = DefaultFormats

  def dispatch(commandLine: CommandLineOptions, d: DatasetApi)(implicit resultOutput: PrintStream): Try[FeedBackMessage] = {
    trace(())
    commandLine.subcommands match {
      case commandLine.dataset :: commandLine.dataset.view :: Nil =>
        for {
          response <- d.view()
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "view"


      // TODO: view-all-versions
      // TODO: export-metadata
      // TODO: list-files
      // TODO: list-metadata-blocks
      // TODO: get-metadata-block
      // TODO: update-metadata
      // TODO: edit-metadata
      // TODO: delete-metadata
      // TODO: publish
      // TODO: delete-draft
      // TODO: set-citation-date-field
      // TODO: revert-citation-date-field
      // TODO: list-role-assignments
      // TODO: assign-role
      // TODO: delete-role-assignment
      // TODO: create-private-url
      // TODO: get-private-url
      // TODO: delete-private-url

      case commandLine.dataset :: commandLine.dataset.addFile :: Nil =>
        for {
          metadata <- if (commandLine.dataset.addFile.metadata()) getStringFromStd
                      else Success("")
          _ = debug(metadata)
          optMeta = if (StringUtils.isBlank(metadata)) Option.empty
                    else Option(metadata)
          optDataFile = commandLine.dataset.addFile.dataFile.map(p => better.files.File(p)).toOption
          response <- d.addFileItem(optDataFile, optMeta)
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "add-file"

      // TODO: storage-size
      // TODO: download-size
      // TODO: submit-for-review
      // TODO: return-to-author
      // TODO: link
      // TODO: get-locks
      // TODO: delete
      // TODO: destroy
      // TODO: await-unlock
      // TODO: await-lock

      case _ => Failure(new RuntimeException(s"Unkown dataverse sub-command: ${ commandLine.args.tail.mkString(" ") }"))
    }
  }
}