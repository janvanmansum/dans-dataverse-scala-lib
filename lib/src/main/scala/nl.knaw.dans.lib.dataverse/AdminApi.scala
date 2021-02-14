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

import nl.knaw.dans.lib.dataverse.model.{ DataMessage, DatabaseSetting, Workflow }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import java.net.URI
import scala.util.Try

class AdminApi private[dataverse](configuration: DataverseInstanceConfig) extends HttpSupport with DebugEnhancedLogging {
  protected val connectionTimeout: Int = configuration.connectionTimeout
  protected val readTimeout: Int = configuration.readTimeout
  protected val baseUrl: URI = configuration.baseUrl
  protected val apiToken: String = configuration.apiToken
  protected val sendApiTokenViaBasicAuth = false
  protected val unblockKey: Option[String] = configuration.unblockKey
  protected val apiPrefix: String = "api/admin"
  protected val apiVersion: Option[String] = Option.empty // No version allowed here

  // TODO: list-database-settings

  /**
   * @see [[https://guides.dataverse.org/en/latest/installation/config.html#database-settings]]
   * @param settingName the name of the setting
   * @param value       the value to set
   * @return
   */
  def putDatabaseSetting(settingName: String, value: String): Try[DataverseResponse[DatabaseSetting]] = {
    trace(settingName, value)
    put[DatabaseSetting](s"settings/${ settingName }", value)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/installation/config.html#database-settings]]
   * @param settingName the name of the setting
   * @param value       the boolean value to set
   * @return
   */
  def putDatabaseSetting(settingName: String, value: Boolean): Try[DataverseResponse[DatabaseSetting]] = {
    trace(settingName, value)
    putDatabaseSetting(settingName, value.toString)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/installation/config.html#database-settings]]
   * @param settingName the name of the setting
   * @return the current value
   */
  def getDatabaseSetting(settingName: String): Try[DataverseResponse[String]] = {
    ???
    // TODO: getDatabaseSetting
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/installation/config.html#database-settings]]
   * @param settingName the name of the setting
   * @return
   */
  def deleteDatabaseSetting(settingName: String): Try[DataverseResponse[Nothing]] = {
    trace(settingName)
    deletePath[Nothing](s"settings/${ settingName }")
  }

  // TODO: add-banner-message
  // TODO: get-banner-messages
  // TODO: delete-banner-message
  // TODO: deactivate-banner-message

  // TODO: list-authentication-provider-factories
  // TODO: list-authentication-providers
  // TODO: add-authentication-provider
  // TODO: view-authentication-provider
  // TODO: set-authentication-provider-enabled (true/false)
  // TODO: is-authentication-provider-enabled (true/false)
  // TODO: delete-authentication-provider

  // TODO: list-roles
  // TODO: create-role

  // TODO: list-users

  /**
   * Returns the account data for a single user.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#list-single-user]]
   * @param id the user ID
   * @return
   */
  def getSingleUser(id: String): Try[DataverseResponse[model.AuthenticatedUser]] = {
    trace(id)
    get[model.AuthenticatedUser](s"authenticatedUsers/$id")
  }

  // TODO: create-user
  // TODO: merge-users
  // TODO: change-user-identifier
  // TODO: make-superuser
  // TODO: delete-user

  // TODO: list-role-assignments (assignee)
  // TODO: list-permissions (user)
  // TODO: view-roles-assignee

  // TODO: list-saved-searches
  // TODO: view-saved-search
  // TODO: make-links (all/id)

  // TODO: fix-missing-unf
  // TODO: compute-datafile-hash
  // TODO: validate-datafile-hash
  // TODO: validate-dataset-datafile-hashes
  // TODO: validate-dataset

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   * @return
   */
  def getWorkflows: Try[DataverseResponse[List[Workflow]]] = {
    trace(())
    get[List[Workflow]]("workflows")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   * @return
   */
  def getWorkflow(id: Int): Try[DataverseResponse[Workflow]] = {
    trace(id)
    get[Workflow](s"workflows/$id")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   * @return
   */
  def addWorkflow(workflow: Workflow): Try[DataverseResponse[Workflow]] = {
    trace(workflow)
    for {
      json <- serializeAsJson(workflow, logger.underlying.isDebugEnabled)
      response <- addWorkflow(json)
    } yield response
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   * @return
   */
  def addWorkflow(json: String): Try[DataverseResponse[Workflow]] = {
    trace(json)
    postJson[Workflow]("workflows", body = json)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   * @return
   */
  def deleteWorkflow(id: Int): Try[DataverseResponse[DataMessage]] = {
    trace(id)
    deletePath[DataMessage](s"workflows/$id")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   * @return
   */
  def getDefaultWorkflows: Try[DataverseResponse[Map[String, Workflow]]] = {
    trace(())
    get[Map[String, Workflow]](s"workflows/default")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   * @param triggerType PrePublishDataset or PostPublishDataset
   * @return
   */
  def getDefaultWorkflow(triggerType: String): Try[DataverseResponse[Workflow]] = {
    trace(triggerType)
    get[Workflow](s"workflows/default/$triggerType")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   * @param triggerType PrePublishDataset or PostPublishDataset
   * @param workflowId  id of the workflow to make default
   * @return
   */
  def setDefaultWorkflow(triggerType: String, workflowId: Int): Try[DataverseResponse[DataMessage]] = {
    trace(triggerType)
    put[DataMessage](s"workflows/default/$triggerType", body = workflowId.toString)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   * @param triggerType PrePublishDataset or PostPublishDataset
   * @return
   */
  def unsetDefaultWorkflow(triggerType: String): Try[DataverseResponse[DataMessage]] = {
    trace(triggerType)
    deletePath[DataMessage](s"workflows/default/$triggerType")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   */
  def setWorkflowsWhitelist(whilelist: String): Try[DataverseResponse[Any]] = {
    trace(whilelist)
    put[Any]("workflows/ip-whitelist", whilelist)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#workflows]]
   */
  def setWorkflowsWhitelist(ips: List[String]): Try[DataverseResponse[Any]] = {
    trace(ips)
    setWorkflowsWhitelist(ips.mkString(";"))
  }

  // TODO: get-workflows-whitelist
  // TODO: delete-workflows-whitelist
  // TODO: clear-metrics-cache [db-name]
  // TODO: add-dataverse-role-assignments-to-children


  /**
   * @see [[https://guides.dataverse.org/en/latest/admin/dataverses-datasets.html#configure-a-dataset-to-store-all-new-files-in-a-specific-file-store]]
   * @return
   */
  def getStorageDrivers: Try[DataverseResponse[Map[String, String]]] = {
    trace(())
    get[Map[String, String]](s"dataverse/storageDrivers")
  }
}
