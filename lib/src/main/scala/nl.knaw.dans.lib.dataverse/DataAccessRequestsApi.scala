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

import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.{ DefaultFormats, Formats }

import java.net.URI
import scala.util.Try

class DataAccessRequestsApi private[dataverse](datasetId: String, isPersistentFileId: Boolean, configuration: DataverseInstanceConfig) extends TargetedHttpSupport with DebugEnhancedLogging {
  trace(())
  private implicit val jsonFormats: Formats = DefaultFormats

  protected val connectionTimeout: Int = configuration.connectionTimeout
  protected val readTimeout: Int = configuration.readTimeout
  protected val baseUrl: URI = configuration.baseUrl
  protected val apiToken: Option[String] = Option(configuration.apiToken)
  protected val sendApiTokenViaBasicAuth = false
  protected val unblockKey: Option[String] = Option.empty
  protected val apiPrefix: String = "api"
  protected val apiVersion: Option[String] = Option(configuration.apiVersion)
  override protected val targetBase: String = "access"
  override protected val id: String = datasetId
  override protected val isPersistentId: Boolean = isPersistentFileId

  def enable(): Try[DataverseResponse[Any]] = {
    trace(())
    putToTarget("allowAccessRequest", "true")
  }

  def disable(): Try[DataverseResponse[Any]] = {
    trace(())
    putToTarget("allowAccessRequest", "false")
  }
}
