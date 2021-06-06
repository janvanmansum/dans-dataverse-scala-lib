/**
 * Copyright (C) 2020 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
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
package nl.knaw.dans.lib.dataverse

import better.files.File
import nl.knaw.dans.lib.dataverse.model.DataMessage
import nl.knaw.dans.lib.dataverse.model.dataset.FileList
import nl.knaw.dans.lib.dataverse.model.file.prestaged.PrestagedFile
import nl.knaw.dans.lib.dataverse.model.file.{ DetectionResult, FileMeta, Provenance }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.native.{ JsonMethods, Serialization }
import org.json4s.{ DefaultFormats, Formats }

import java.net.URI
import java.nio.charset.StandardCharsets
import scala.util.{ Failure, Try }
import scala.xml.Elem

class FileApi private[dataverse](filedId: String, isPersistentFileId: Boolean, configuration: DataverseInstanceConfig) extends TargetedHttpSupport with DebugEnhancedLogging {
  protected val connectionTimeout: Int = configuration.connectionTimeout
  protected val readTimeout: Int = configuration.readTimeout
  protected val baseUrl: URI = configuration.baseUrl
  protected val apiToken: Option[String] = Option(configuration.apiToken)
  protected val sendApiTokenViaBasicAuth = false
  protected val unblockKey: Option[String] = Option.empty
  protected val apiPrefix: String = "api"
  protected val apiVersion: Option[String] = Option(configuration.apiVersion)

  protected val targetBase: String = "files"
  protected val id: String = filedId
  protected val isPersistentId: Boolean = isPersistentFileId

  private implicit val jsonFormats: Formats = DefaultFormats

  // TODO: download

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#restrict-files]]
   * @param restrict `true` if the file must have restricted access, `false` for open access
   * @return
   */
  def restrict(restrict: Boolean): Try[DataverseResponse[DataMessage]] = {
    trace(restrict)
    putToTarget("restrict", restrict.toString)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#uningest-a-file]]
   * @return
   */
  def uningest(): Try[DataverseResponse[DataMessage]] = {
    trace(())
    // Not really JSON we are posting, but this seem to work anyway.
    postJsonToTarget("uningest", "")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#reingest-a-file]]
   * @return
   */
  def reingest(): Try[DataverseResponse[DataMessage]] = {
    trace(())
    postJsonToTarget("reingest", "")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#redetect-file-type]]
   * @param dryRun do not save the change
   * @return
   */
  def redetect(dryRun: Boolean = false): Try[DataverseResponse[DetectionResult]] = {
    trace(dryRun)
    postJsonToTarget("redetect", "", Map("dryRun" -> dryRun.toString))
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#replacing-files]]
   * @param optDataFile           the replacement file
   * @param optFileMetadata the replacement metadata
   * @return
   */
  def replaceFileItem(optDataFile: Option[File] = Option.empty, optFileMetadata: Option[String] = Option.empty): Try[DataverseResponse[FileList]] = {
    trace(optDataFile, optFileMetadata)
    if (optDataFile.isEmpty && optFileMetadata.isEmpty) Failure(new IllegalArgumentException("At least one of file data and file metadata must be provided."))
    postFileToTarget("replace", optDataFile, optFileMetadata)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#replacing-files]]
   * @param optDataFile           the replacement file
   * @param optFileMetadata the replacement metadata
   * @return
   */
  def replace(optDataFile: Option[File] = Option.empty, optFileMetadata: Option[FileMeta] = Option.empty): Try[DataverseResponse[FileList]] = {
    trace(optDataFile, optFileMetadata)
    replaceFileItem(optDataFile, optFileMetadata.map(m => Serialization.writePretty(m)))
  }

  /**
   * Replaces a file with a pre-staged file.
   *
   * @see [[https://guides.dataverse.org/en/latest/developers/s3-direct-upload-api.html#replacing-an-existing-file-in-the-dataset]]
   *
   * @param prestagedFile metadata about the prestaged file
   * @return
   */
  def replaceWithPrestagedFile(prestagedFile: PrestagedFile): Try[DataverseResponse[FileList]] = {
    trace(prestagedFile)
    replaceFileItem(Option.empty, Option(Serialization.write(prestagedFile)))
  }


  /**
   * Note: for some reason, the Dataverse's response is not wrapped in the usual envelope here.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#getting-file-metadata]]
   * @return
   */
  def getMetadata: Try[FileMeta] = {
    trace(())
    for {
      response <- getUnwrappedFromTarget("metadata")
      bodyString <- Try { new String(response.body, StandardCharsets.UTF_8) }
      json <- Try { JsonMethods.parse(bodyString) }
      fileMeta <- Try { json.extract[FileMeta] }
    } yield fileMeta
  }

  /**
   * Unfortunately, the body of the response is not valid JSON, hence the `Nothing` payload type.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#updating-file-metadata]]
   * @param fm file metadata
   * @return
   */
  def updateMetadata(fm: String): Try[DataverseResponse[Nothing]] = {
    trace(fm)
    postFileToTarget[Nothing]("metadata", optFile = None, optMetadata = Option(fm))
  }

  /**
   * Unfortunately, the body of the response is not valid JSON, hence the `Nothing` payload type.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#updating-file-metadata]]
   * @param fm file metadata
   * @return
   */
  def updateMetadata(fm: FileMeta): Try[DataverseResponse[Nothing]] = {
    trace(fm)
    updateMetadata(Serialization.write(fm))
  }

  // TODO: describe requirements for provided xml

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#editing-variable-level-metadata]]
   * @param ddiXml DDI xml
   * @return
   */
  def editVariableMetadata(ddiXml: Elem): Try[DataverseResponse[Any]] = {
    // TODO: editVariableMetadata
    ???
  }

  /**
   * Returns JSON-based provenance.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#get-provenance-json-for-an-uploaded-file]]
   * @return
   */
  def getProvenanceJson: Try[DataverseResponse[Any]] = {
    ???
    // TODO: getProvenanceJson
  }

  /**
   * Returns free-form provenance.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#get-provenance-description-for-an-uploaded-file]]
   * @return
   */
  def getProvenanceDescription: Try[DataverseResponse[Any]] = {
    // TODO: getProvenanceDescription
    ???
  }

  /**
   * Sets JSON-based provenance.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#create-update-provenance-json-and-provide-related-entity-name-for-an-uploaded-file]]
   * @param p provenance to set
   * @return
   */
  def putProvenanceJson(p: Provenance): Try[DataverseResponse[Any]] = {
    ???
    // TODO: putProvenanceJson
  }

  /**
   * Sets free-form provenance.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#create-update-provenance-description-for-an-uploaded-file]]
   */
  def putProvenanceDescription(p: String): Try[DataverseResponse[Any]] = {
    ???
    // TODO: putProvenanceDescription
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#delete-provenance-json-for-an-uploaded-file]]
   * @return
   */
  def deleteProvenanceJson(): Try[DataverseResponse[Any]] = {
    ???
    // TODO: deleteProvenanceJson
  }

  /*
   * Datafile Integrity / fixmissingoriginalsizes  not implemented. It is not targeted at a specific file, so this is not the correct place.
   * Also, it seems it was a one-off function to fix old installations, so of diminishing relevance anyway.
   */
}
