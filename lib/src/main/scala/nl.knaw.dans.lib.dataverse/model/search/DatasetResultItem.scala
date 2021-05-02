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
package nl.knaw.dans.lib.dataverse.model.search

case class DatasetResultItem(override val `type`: String,
                             override val name: String,
                             override val url: String,
                             globalId: String,
                             description: String,
                             publishedAt: String,
                             publisher: String,
                             citationHtml: String,
                             identifierOfDataverse: String,
                             nameOfDataverse: String,
                             citation: String,
                             storageIdentifier: String,
                             subjects: List[String],
                             fileCount: Int,
                             versionId: Int,
                             versionState: String,
                             majorVersion: Int,
                             minorVersion: Int,
                             createdAt: String,
                             updatedAt: String,
                             contacts: List[Map[String, String]],
                             authors: List[String]
                            ) extends ResultItem(`type`, name, url) {
}
