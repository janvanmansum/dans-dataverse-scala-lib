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
import nl.knaw.dans.lib.dataverse.model._
import nl.knaw.dans.lib.dataverse.model.dataset.UpdateType.UpdateType
import nl.knaw.dans.lib.dataverse.model.dataset.{ DatasetLatestVersion, DatasetVersion, FieldList, FileList, MetadataBlock, MetadataBlocks, PrivateUrlData }
import nl.knaw.dans.lib.dataverse.model.file.FileMeta
import nl.knaw.dans.lib.dataverse.model.file.prestaged.PrestagedFile
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats }

import java.lang.Thread.sleep
import java.net.URI
import scala.util.{ Failure, Success, Try }

/**
 * Functions that operate on a single dataset. See [[https://guides.dataverse.org/en/latest/api/native-api.html#datasets]].
 *
 */
class DatasetApi private[dataverse](datasetId: String, isPersistentDatasetId: Boolean, configuration: DataverseInstanceConfig, workflowId: Option[String] = None) extends TargetedHttpSupport with DebugEnhancedLogging {
  private implicit val jsonFormats: Formats = DefaultFormats
  private val HEADER_DATAVERSE_INVOCATION_ID = "X-Dataverse-invocationID"

  protected val connectionTimeout: Int = configuration.connectionTimeout
  protected val readTimeout: Int = configuration.readTimeout
  protected val baseUrl: URI = configuration.baseUrl
  protected val apiToken: Option[String] = if (workflowId.isDefined) Option.empty
                                           else Option(configuration.apiToken)
  protected val sendApiTokenViaBasicAuth = false
  protected val unblockKey: Option[String] = Option.empty
  protected val apiPrefix: String = "api"
  protected val apiVersion: Option[String] = Option(configuration.apiVersion)
  protected val awaitLockStateMaxNumberOfRetries: Int = configuration.awaitLockStateMaxNumberOfRetries
  protected val awaitLockStateMillisecondsBetweenRetries: Int = configuration.awaitLockStateMillisecondsBetweenRetries

  protected val targetBase: String = "datasets"
  protected val id: String = datasetId
  protected val isPersistentId: Boolean = isPersistentDatasetId
  override protected val extraHeaders: Map[String, String] = workflowId.map(wfid => Map(HEADER_DATAVERSE_INVOCATION_ID -> wfid)).getOrElse(Map.empty)

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#get-json-representation-of-a-dataset]]
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#get-version-of-a-dataset]]
   * @param version version to view (optional)
   * @return
   */
  def view(version: Version = Version.LATEST): Try[DataverseResponse[DatasetVersion]] = {
    trace(version)
    getVersionedFromTarget[DatasetVersion]("", version)
  }

  /**
   * Almost the same as [[DatasetApi#view]] except that `viewLatestVersion` returns a JSON object that starts at the dataset
   * level instead of the dataset version level. The dataset level contains some fields, most of which are replicated at the dataset version level, however.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#get-json-representation-of-a-dataset]]
   * @return
   */
  def viewLatestVersion(): Try[DataverseResponse[DatasetLatestVersion]] = {
    trace(())
    getUnversionedFromTarget[DatasetLatestVersion]("")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#list-versions-of-a-dataset]]
   * @return
   */
  def viewAllVersions(): Try[DataverseResponse[List[DatasetVersion]]] = {
    trace(())
    getUnversionedFromTarget[List[DatasetVersion]]("versions")
  }

  /**
   * Since the export format is generally not JSON you cannot use the [[DataverseResponse#json]] and [[DataverseResponse#data]]
   * on the result. You should instead use [[DataverseResponse#string]].
   *
   * Note that this API does not support specifying a version.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#export-metadata-of-a-dataset-in-various-formats]]
   * @param format the export format
   * @return
   */
  def exportMetadata(format: String): Try[DataverseResponse[Any]] = {
    trace(())
    if (!isPersistentId) Failure(new IllegalArgumentException("exportMetadata only works with PIDs"))
    // Cannot use helper function because this API does not support the :persistentId constant
    get[Any](s"datasets/export/?exporter=$format&persistentId=$id")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#list-files-in-a-dataset]]
   * @param version the version of the dataset
   * @return
   */
  def listFiles(version: Version = Version.LATEST): Try[DataverseResponse[List[FileMeta]]] = {
    trace(version)
    getVersionedFromTarget[List[FileMeta]]("files", version)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#list-all-metadata-blocks-for-a-dataset]]
   * @param version the version of the dataset
   * @return a map of metadata block identifier to metadata block
   */
  def listMetadataBlocks(version: Version = Version.LATEST): Try[DataverseResponse[MetadataBlocks]] = {
    trace((version))
    getVersionedFromTarget[MetadataBlocks]("metadata", version)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#list-single-metadata-block-for-a-dataset]]
   * @param name    the metadata block identifier
   * @param version the version of the dataset
   * @return
   */
  def getMetadataBlock(name: String, version: Version = Version.LATEST): Try[DataverseResponse[MetadataBlock]] = {
    trace(name, version)
    getVersionedFromTarget[MetadataBlock](s"metadata/$name", version)
  }

  /**
   * Creates or overwrites the current draft's metadata completely.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#update-metadata-for-a-dataset]]
   * @param s JSON document containing the updated metadata blocks
   * @return
   */
  def updateMetadata(s: String): Try[DataverseResponse[DatasetVersion]] = {
    trace(s)
    // Cheating with endPoint here, because the only version that can be updated is :draft anyway
    putToTarget[DatasetVersion]("versions/:draft", s)
  }

  /**
   * Creates or overwrites the current draft's metadata completely.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#update-metadata-for-a-dataset]]
   * @param metadataBlocks map from metadata block id to `MetadataBlock`
   * @return
   */
  def updateMetadata(metadataBlocks: MetadataBlocks): Try[DataverseResponse[DatasetVersion]] = {
    trace(metadataBlocks)
    updateMetadata(Serialization.write(Map("metadataBlocks" -> metadataBlocks)))
  }

  /**
   * Edits the current draft's metadata, adding the fields that do not exist yet. If `replace` is set to `false`, all specified
   * fields must be either currently empty or allow multiple values. Replaces existing data.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#edit-dataset-metadata]]
   * @param s JSON document containing the edits to perform
   * @return
   */
  def editMetadata(s: String): Try[DataverseResponse[DatasetVersion]] = {
    trace(s)
    editMetadata(s, true)
  }

  /**
   * Edits the current draft's metadata, adding the fields that do not exist yet. If `replace` is set to `false`, all specified
   * fields must be either currently empty or allow multiple values.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#edit-dataset-metadata]]
   * @param s       JSON document containing the edits to perform
   * @param replace whether to replace existing values
   * @return
   */
  def editMetadata(s: String, replace: Boolean): Try[DataverseResponse[DatasetVersion]] = {
    trace(s, replace)
    putToTarget("editMetadata",
      s,
      if (replace) Map("replace" -> "true")
      else Map.empty) // Sic! any value for "replace" is interpreted by Dataverse as "true", even "replace=false"
  }

  /**
   * Edits the current draft's metadata, adding the fields that do not exist yet. If `replace` is set to `false`, all specified
   * fields must be either currently empty or allow multiple values.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#edit-dataset-metadata]]
   * @param fields  list of fields to edit
   * @param replace whether to replace existing values
   * @return
   */
  def editMetadata(fields: FieldList, replace: Boolean = true): Try[DataverseResponse[DatasetVersion]] = {
    trace(fields)
    editMetadata(Serialization.write(fields), replace)
  }

  /**
   * Deletes one or more values from the current draft's metadata. Note that the delete will fail if the
   * result would leave required fields empty.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#delete-dataset-metadata]]
   * @param s JSON document describing what to delete
   * @return
   */
  def deleteMetadata(s: String): Try[DataverseResponse[DatasetVersion]] = {
    trace(s)
    putToTarget("deleteMetadata", s)
  }

  /**
   * Deletes one or more values from the current draft's metadata. Note that the delete will fail if the
   * result would leave required fields empty.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#delete-dataset-metadata]]
   * @param fields the fields to delete
   * @return
   */
  def deleteMetadata(fields: FieldList): Try[DataverseResponse[DatasetVersion]] = {
    trace(fields)
    deleteMetadata(Serialization.write(fields))
  }

  /**
   * Publishes the current draft of a dataset as a new version.
   *
   * If publish is called shortly after a modification and there is a pre-publication workflow installed, there is a risk of the workflow failing to
   * start because of an OptimisticLockException. This is caused by Dataverse indexing the dataset on a separate thread. This will appear to the client
   * as Dataverse silently failing (i.e. returning success but not publishing the dataset). To make sure that indexing has already happened the `assureIsIndexed`
   * parameter is set to `true`. It will cause Dataverse to fail fast if indexing is still pending.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#publish-a-dataset]]
   * @param updateType      major or minor version update
   * @param assureIsIndexed make Dataverse return 409 Conflict if an index action is pending
   * @return
   */
  def publish(updateType: UpdateType, assureIsIndexed: Boolean = true): Try[DataverseResponse[DatasetPublicationResult]] = {
    trace(updateType)
    postJsonToTarget[DatasetPublicationResult]("actions/:publish", "", Map("type" -> updateType.toString, "assureIsIndexed" -> assureIsIndexed.toString))
  }

  /**
   * Publishes the current draft of an imported dataset as a new version with the original publication date.
   *
   * If publish is called shortly after a modification and there is a pre-publication workflow installed, there is a risk of the workflow failing to
   * start because of an OptimisticLockException. This is caused by Dataverse indexing the dataset on a separate thread. This will appear to the client
   * as Dataverse silently failing (i.e. returning success but not publishing the dataset). To make sure that indexing has already happened the `assureIsIndexed`
   * parameter is set to `true`. It will cause Dataverse to fail fast if indexing is still pending.
   *
   * @param publicationDateJsonLd original publication date
   * @param assureIsIndexed       make Dataverse return 409 Conflict if an index action is pending
   * @return
   */
  def releaseMigrated(publicationDateJsonLd: String, assureIsIndexed: Boolean = true): Try[DataverseResponse[DatasetPublicationResult]] = {
    trace(publicationDateJsonLd)
    postJsonToTarget[DatasetPublicationResult]("actions/:releasemigrated", publicationDateJsonLd, queryParams = Map("assureIsIndexed" -> assureIsIndexed.toString), isJsonLd = true)
  }

  /**
   * Deletes the current draft of a dataset.
   *
   * Note: as of writing this there is a bug in Dataverse (v5.1.1) which causes it to use the literal string `:persistendId` in the response message
   * instead of the actual identifier when using a PID.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#delete-dataset-draft]]
   * @return
   */
  def deleteDraft(): Try[DataverseResponse[DataMessage]] = {
    trace(())
    deleteAtTarget("versions/:draft")
  }

  /**
   * Sets the dataset citation date field type for a given dataset.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#set-citation-date-field-type-for-a-dataset]]
   * @param fieldName the field name of a date field
   * @return
   */
  def setCitationDateField(fieldName: String): Try[DataverseResponse[Nothing]] = {
    trace(fieldName)
    putToTarget("citationdate", fieldName)
  }

  /**
   * Restores the default citation date field type.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#revert-citation-date-field-type-to-default-for-dataset]]
   * @return
   */
  def revertCitationDateField(): Try[DataverseResponse[Nothing]] = {
    deleteAtTarget("citationdate")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#list-role-assignments-in-a-dataset]]
   * @return
   */
  def listRoleAssignments(): Try[DataverseResponse[List[RoleAssignmentReadOnly]]] = {
    trace(())
    getUnversionedFromTarget("assignments")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#assign-a-new-role-on-a-dataset]]
   * @param s JSON document describing the assignment
   * @return
   */
  def assignRole(s: String): Try[DataverseResponse[RoleAssignmentReadOnly]] = {
    trace(s)
    postJsonToTarget[RoleAssignmentReadOnly]("assignments", s)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#assign-a-new-role-on-a-dataset]]
   * @param roleAssignment object describing the assignment
   * @return
   */
  def assignRole(roleAssignment: RoleAssignment): Try[DataverseResponse[RoleAssignmentReadOnly]] = {
    trace(roleAssignment)
    assignRole(Serialization.write(roleAssignment))
  }

  /**
   * Use [[DatasetApi#listRoleAssignments]] to get the ID.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#delete-role-assignment-from-a-dataset]]
   * @param assignmentId the ID of the assignment to delete
   * @return
   */
  def deleteRoleAssignment(assignmentId: Int): Try[DataverseResponse[Nothing]] = {
    trace(assignmentId)
    deleteAtTarget[Nothing](s"assignments/${ assignmentId }")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#create-a-private-url-for-a-dataset]]
   * @return
   */
  def createPrivateUrl(): Try[DataverseResponse[PrivateUrlData]] = {
    trace(())
    postJsonToTarget[PrivateUrlData]("privateUrl", "")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#get-the-private-url-for-a-dataset]]
   * @return
   */
  def getPrivateUrl: Try[DataverseResponse[PrivateUrlData]] = {
    trace(())
    getUnversionedFromTarget("privateUrl")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#delete-the-private-url-from-a-dataset]]
   * @return
   */
  def deletePrivateUrl(): Try[DataverseResponse[Nothing]] = {
    trace(())
    deleteAtTarget[Nothing]("privateUrl")
  }

  /**
   * This function is called addFileItem instead of simply addFile to avoid naming conflicts with the other addFile function. The only difference is that
   * this function takes the metadata as a string, whereas addFile takes it as a model object.
   *
   * @see [[  https://guides.dataverse.org/en/latest/api/native-api.html#add-a-file-to-a-dataset]]
   * @param optDataFile     optional file data to upload
   * @param optFileMetadata optional metadata as a JSON string
   * @return
   */
  def addFileItem(optDataFile: Option[File] = Option.empty, optFileMetadata: Option[String] = Option.empty): Try[DataverseResponse[FileList]] = {
    trace(optDataFile, optFileMetadata)
    if (optDataFile.isEmpty && optFileMetadata.isEmpty) Failure(new IllegalArgumentException("At least one of file data and file metadata must be provided."))
    postFileToTarget[FileList]("add", optDataFile, optFileMetadata)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#add-a-file-to-a-dataset]]
   * @param optDataFile     optional file data to upload
   * @param optFileMetadata optional metadata for the file
   * @return
   */
  def addFile(optDataFile: Option[File] = Option.empty, optFileMetadata: Option[FileMeta] = Option.empty): Try[DataverseResponse[FileList]] = {
    trace(optDataFile, optFileMetadata)
    if (optDataFile.isEmpty && optFileMetadata.isEmpty) Failure(new IllegalArgumentException("At least one of file data and file metadata must be provided."))
    addFileItem(optDataFile, optFileMetadata.map(fm => Serialization.write(fm)))
  }

  /**
   * Adds a pre-staged file
   *
   * @see [[https://guides.dataverse.org/en/latest/developers/s3-direct-upload-api.html#adding-the-uploaded-file-to-the-dataset]]
   *
   * @param prestagedFile metadata about the prestaged file
   * @return
   */
  def addPrestagedFile(prestagedFile: PrestagedFile): Try[DataverseResponse[FileList]] = {
    trace(prestagedFile)
    addFileItem(Option.empty, Option(Serialization.write(prestagedFile)))
  }


  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#report-the-data-file-size-of-a-dataset]]
   * @return
   */
  def getStorageSize(includeCached: Boolean = false): Try[DataverseResponse[DataMessage]] = {
    trace(())
    getUnversionedFromTarget[DataMessage]("storagesize", Map("includeCached" -> includeCached.toString))
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#get-the-size-of-downloading-all-the-files-of-a-dataset-version]]
   * @return
   */
  def getDownloadSize(version: Version = Version.LATEST): Try[DataverseResponse[DataMessage]] = {
    trace(())
    getVersionedFromTarget[DataMessage]("downloadsize", version)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#submit-a-dataset-for-review]]
   * @return
   */
  def submitForReview(): Try[DataverseResponse[DataMessage]] = {
    trace(())
    postJsonToTarget[DataMessage]("submitForReview", "")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#return-a-dataset-to-author]]
   * @param s JSON document containing the reason for returning the dataset
   * @return
   */
  def returnToAuthor(s: String): Try[DataverseResponse[DataMessage]] = {
    trace(())
    postJsonToTarget[DataMessage]("returnToAuthor", s)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#link-a-dataset]]
   * @param targetDataverse alias of the dataverse in which the link to the dataset should appear
   * @return
   */
  def link(targetDataverse: String): Try[DataverseResponse[DataMessage]] = {
    trace(())
    putToTarget[DataMessage](s"link/$targetDataverse", "")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#dataset-locks]]
   * @return
   */
  def getLocks: Try[DataverseResponse[List[Lock]]] = {
    trace(())
    getUnversionedFromTarget[List[Lock]]("locks")
  }

  // TODO: metrics. First install/enable Make Data Count ?

  /**
   * Note: delete on a published dataset with one version also works, if you are a superuser.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#delete-unpublished-dataset]]
   * @return
   */
  def delete(): Try[DataverseResponse[DataMessage]] = {
    trace(())
    deleteAtTarget[DataMessage]("")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#delete-published-dataset]]
   * @return
   */
  def destroy(): Try[DataverseResponse[DataMessage]] = {
    trace(())
    deleteAtTarget[DataMessage]("destroy")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/admin/dataverses-datasets.html#configure-a-dataset-to-store-all-new-files-in-a-specific-file-store]]
   * @return
   */
  def getStorageDriver: Try[DataverseResponse[DataMessage]] = {
    trace(())
    getUnversionedFromTarget[DataMessage]("storageDriver")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/admin/dataverses-datasets.html#configure-a-dataset-to-store-all-new-files-in-a-specific-file-store]]
   * @see driver the label of the storage driver to use
   * @return
   */
  def setStorageDriver(driver: String): Try[DataverseResponse[DataMessage]] = {
    trace(())
    putToTarget[DataMessage]("storageDriver", driver)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/admin/dataverses-datasets.html#configure-a-dataset-to-store-all-new-files-in-a-specific-file-store]]
   * @return
   */
  def resetStorageDriver(): Try[DataverseResponse[DataMessage]] = {
    trace(())
    deleteAtTarget[DataMessage]("storageDriver")
  }

  /**
   * Utility function that lets you wait until all locks are cleared before proceeding. Unlike most other functions
   * in this library, this does not correspond directly with an API call. Rather the [[getLocks]] call is done repeatedly
   * to check if the locks have been cleared. Note that in scenarios where concurrent processes might access the same dataset
   * it is not guaranteed that the locks, once cleared, stay that way.
   *
   * @param maxNumberOfRetries     the maximum number the check for unlock is made, defaults to [[awaitLockStateMaxNumberOfRetries]]
   * @param waitTimeInMilliseconds the time between tries, defaults to [[awaitLockStateMillisecondsBetweenRetries]]
   * @return
   */
  def awaitUnlock(maxNumberOfRetries: Int = awaitLockStateMaxNumberOfRetries, waitTimeInMilliseconds: Int = awaitLockStateMillisecondsBetweenRetries): Try[Unit] = {
    trace(maxNumberOfRetries, waitTimeInMilliseconds)
    awaitLockState(_.isEmpty, "Wait for unlock expired", maxNumberOfRetries, waitTimeInMilliseconds)
  }

  /**
   * Utility function that lets you wait until a specified lock type is set. Unlike most other functions
   * in this library, this does not correspond directly with an API call. Rather the [[getLocks]] call is done repeatedly
   * to check if the locks has been set. A use case is when an http/sr workflow wants to make sure that a dataset has been
   * locked on its behalf, so that it can be sure to have exclusive access via its invocation ID.
   *
   * @param lockType               the lock type to wait for
   * @param maxNumberOfRetries     the maximum number the check for unlock is made, defaults to [[awaitLockStateMaxNumberOfRetries]]
   * @param waitTimeInMilliseconds the time between tries, defaults to [[awaitLockStateMillisecondsBetweenRetries]]
   * @return
   */
  def awaitLock(lockType: String, maxNumberOfRetries: Int = awaitLockStateMaxNumberOfRetries, waitTimeInMilliseconds: Int = awaitLockStateMillisecondsBetweenRetries): Try[Unit] = {
    trace(maxNumberOfRetries, waitTimeInMilliseconds)
    awaitLockState(_.exists(_.lockType == lockType), s"Wait for lock of type $lockType expired", maxNumberOfRetries, waitTimeInMilliseconds)
  }

  /**
   * Helper function that waits until the specified lockState function returns `true`, or throws a LockException if this never occurs
   * within `maxNumberOrRetries` with `waitTimeInMilliseconds` pauses.
   *
   * @param lockState              the function that returns whether the required state has been reached
   * @param errorMessage           error to report in LockException if it occurs
   * @param maxNumberOfRetries     the maximum number of tries
   * @param waitTimeInMilliseconds the time to wait between tries
   * @return
   */
  private def awaitLockState(lockState: List[Lock] => Boolean, errorMessage: String, maxNumberOfRetries: Int = awaitLockStateMaxNumberOfRetries, waitTimeInMilliseconds: Int = awaitLockStateMillisecondsBetweenRetries): Try[Unit] = {
    trace(maxNumberOfRetries, waitTimeInMilliseconds)
    var numberOfTimesTried = 0

    def getCurrentLocks: Try[List[Lock]] = {
      for {
        response <- getLocks
        locks <- response.data
        _ = debug(s"Current locks: ${ locks.mkString(", ") }")
      } yield locks
    }

    def slept(): Boolean = {
      debug(s"Sleeping $waitTimeInMilliseconds ms before next try..")
      sleep(waitTimeInMilliseconds)
      true
    }

    var maybeLocks = getCurrentLocks
    do {
      maybeLocks = getCurrentLocks
      numberOfTimesTried += 1
    } while (maybeLocks.isSuccess && !lockState(maybeLocks.get) && numberOfTimesTried != maxNumberOfRetries && slept())

    if (maybeLocks.isFailure) maybeLocks.map(_ => ())
    else if (!lockState(maybeLocks.get)) Failure(LockException(numberOfTimesTried, waitTimeInMilliseconds, errorMessage))
         else Success(())
  }
}
