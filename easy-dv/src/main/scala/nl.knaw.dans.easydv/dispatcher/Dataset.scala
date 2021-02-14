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
import nl.knaw.dans.lib.dataverse.model.dataset.UpdateType
import nl.knaw.dans.lib.dataverse.{ DatasetApi, Version }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.lang.StringUtils
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats }

import java.io.PrintStream
import scala.language.reflectiveCalls
import scala.util.{ Failure, Success, Try }

object Dataset extends DebugEnhancedLogging {
  private implicit val jsonFormats: Formats = DefaultFormats

  def dispatch(commandLine: CommandLineOptions, d: DatasetApi, datasetSetVersion: Option[Version])(implicit resultOutput: PrintStream): Try[FeedBackMessage] = {
    trace(())
    commandLine.subcommands match {
      case (ds @ commandLine.dataset) :: commandLine.dataset.view :: Nil =>
        for {
          response <-
            if (ds.all()) d.viewAllVersions()
            else d.view(version = datasetSetVersion.getOrElse(Version.LATEST))
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "view"
      case commandLine.dataset :: (c @ commandLine.dataset.exportMetadata) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else if (List("schema.org", "OAI_ORE", "dataverse_json").contains(c.format())) {
          for {
            response <- d.exportMetadata(c.format())
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "export-metadata"
        }
             else {
               for {
                 response <- d.exportMetadata(c.format())
                 s <- response.string
                 _ = resultOutput.println(s)
               } yield "export-metadata"
             }
      case commandLine.dataset :: commandLine.dataset.listFiles :: Nil =>
        for {
          response <- d.listFiles(datasetSetVersion.getOrElse(Version.LATEST))
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "list-files"
      case commandLine.dataset :: commandLine.dataset.listMetadataBlocks :: Nil =>
        for {
          response <- d.listMetadataBlocks(datasetSetVersion.getOrElse(Version.LATEST))
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "list-metadata-blocks"
      case commandLine.dataset :: (c @ commandLine.dataset.getMetadataBlock) :: Nil =>
        for {
          response <- d.getMetadataBlock(c.name(), datasetSetVersion.getOrElse(Version.LATEST))
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "get-metadata-block"
      case commandLine.dataset :: (c @ commandLine.dataset.updateMetadata) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            s <- getStringFromStd
            response <- d.updateMetadata(s)
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "update-metadata"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.editMetadata) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            s <- getStringFromStd
            response <- d.editMetadata(s, c.replace())
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "edit-metadata"
        }
      case commandLine.dataset :: commandLine.dataset.deleteMedata :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            s <- getStringFromStd
            response <- d.deleteMetadata(s)
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "delete-metadata"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.publish) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.publish(updateType = if (c.major()) UpdateType.major
                                               else UpdateType.minor)
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "publish"
        }
      case commandLine.dataset :: commandLine.dataset.deleteDraft :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.deleteDraft()
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "delete-draft"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.setCitationDateField) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.setCitationDateField(c.field())
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "set-citation-date-field"
        }
      case commandLine.dataset :: commandLine.dataset.revertCitationDateField :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.revertCitationDateField()
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "revert-citation-date-field"
        }
      case commandLine.dataset :: commandLine.dataset.listRoleAssignments :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.listRoleAssignments()
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "list-role-assignments"
        }
      case commandLine.dataset :: commandLine.dataset.assignRole :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            s <- getStringFromStd
            response <- d.assignRole(s)
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "assign-role"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.deleteRoleAssignment) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.deleteRoleAssignment(c.id())
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "delete-role-assignment"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.createPrivateUrl) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.createPrivateUrl()
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "create-private-url"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.getPrivateUrl) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.getPrivateUrl
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "get-private-url"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.deletePrivateUrl) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.deletePrivateUrl()
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "delete-private-url"
        }
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
      case commandLine.dataset :: (c @ commandLine.dataset.storageSize) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.getStorageSize(c.includeCached())
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "storage-size"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.downloadSize) :: Nil =>
        for {
          response <- d.getDownloadSize(datasetSetVersion.getOrElse(Version.LATEST))
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "download-size"
      case commandLine.dataset :: commandLine.dataset.submitForReview :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.submitForReview()
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "submit-for-review"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.returnToAuthor) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.returnToAuthor(
              s"""
                | {
                |   "reasonForReturn": "${c.reason()}"
                | }
                |""".stripMargin)
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "return-to-author"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.link) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.link(c.targetDataverse())
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "link"
        }
      case commandLine.dataset :: commandLine.dataset.getLocks :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.getLocks
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "get-locks"
        }
      case commandLine.dataset :: commandLine.dataset.delete :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.delete()
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "delete"
        }
      case commandLine.dataset :: commandLine.dataset.destroy :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            response <- d.destroy()
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "destroy"
        }
      case commandLine.dataset :: (c @ commandLine.dataset.awaitLock) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            _ <- d.awaitLock(c.lockType())
          } yield "await-lock"
        }
      case commandLine.dataset :: commandLine.dataset.awaitUnlock :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            _ <- d.awaitUnlock()
          } yield "await-unlock"
        }
      case _ => Failure(new RuntimeException(s"Unkown dataverse sub-command: ${ commandLine.args.tail.mkString(" ") }"))
    }
  }
}