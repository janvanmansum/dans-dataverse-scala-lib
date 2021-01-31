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

import java.net.URI
import scala.util.Try

class WorkflowsApi private[dataverse](configuration: DataverseInstanceConfig) extends HttpSupport with DebugEnhancedLogging {
  protected val connectionTimeout: Int = configuration.connectionTimeout
  protected val readTimeout: Int = configuration.readTimeout
  protected val baseUrl: URI = configuration.baseUrl
  protected val apiToken: String = configuration.apiToken
  protected val sendApiTokenViaBasicAuth = false
  protected val unblockKey: Option[String] = Option.empty
  protected val apiPrefix: String = "api"
  protected val apiVersion: Option[String] = Option(configuration.apiVersion)

  /**
   * Resumes a paused workflow.
   *
   * @see [[https://guides.dataverse.org/en/latest/developers/workflows.html#introduction]]
   * @see [[https://guides.dataverse.org/en/latest/developers/workflows.html#pause]]
   * @param invocationId the invocation ID of the workflow to resume
   * @param fail         if `true` makes the workflow fail
   * @return
   */
  def resume(invocationId: String, fail: Boolean = false): Try[DataverseResponse[Nothing]] = {
    trace(invocationId, fail)
    postText(s"workflows/$invocationId", body = if (fail) "fail"
                                                else "")
  }
}
