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
import nl.knaw.dans.lib.dataverse.FileApi
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.{ DefaultFormats, Formats }

import java.io.PrintStream
import scala.util.{ Failure, Try }

object File extends DebugEnhancedLogging {
  private implicit val jsonFormats: Formats = DefaultFormats

  def dispatch(commandLine: CommandLineOptions, f: FileApi)(implicit resultOutput: PrintStream): Try[FeedBackMessage] = {
    trace(())
    commandLine.subcommands match {
      case commandLine.file :: commandLine.file.`updateMetadata` :: Nil =>
        for {
          fm <- getStringFromStd
          response <- f.updateMetadata(fm)
          output <- response.string // Result contains non-JSON prefix, so we cannot parse and prettyprint here.
          _ = resultOutput.println(output)
        } yield "update-metadata"
      case _ => Failure(new RuntimeException(s"Unkown dataverse sub-command: ${ commandLine.args.tail.mkString(" ") }"))
    }
  }
}
