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

import better.files.File
import nl.knaw.dans.easydv.dispatcher.Dataverse
import nl.knaw.dans.lib.dataverse.{ DataverseException, DataverseInstance }
import nl.knaw.dans.lib.error.TryExtensions
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import java.io.PrintStream
import scala.language.{ postfixOps, reflectiveCalls }
import scala.util.Try

object Command extends App with DebugEnhancedLogging {
  type FeedBackMessage = String
  implicit val resultOutput: PrintStream = Console.out

  val configuration = Configuration(File(System.getProperty("app.home")))
  logger.debug(s"Read configuration: $configuration")
  val commandLine: CommandLineOptions = new CommandLineOptions(args, configuration) {
    verify()
  }
  val instance = new DataverseInstance(configuration.dvConfig)

  val result: Try[FeedBackMessage] = commandLine.subcommands match {
    case commandLine.dataverse :: _ => Dataverse.dispatch(commandLine, instance.dataverse(commandLine.dataverse.alias()))
  }

  result.doIfSuccess(msg => Console.err.println(s"OK: $msg"))
    .doIfFailure {
      case de: DataverseException =>
        Console.err.println(s"ERROR: ${ de.getMessage }")
        System.exit(1)
      case t =>
        Console.err.println(s"ERROR: ${ t.getClass.getSimpleName }: ${ t.getMessage }")
        logger.error("A fatal exception occurred", t)
        System.exit(1)
    }
}

