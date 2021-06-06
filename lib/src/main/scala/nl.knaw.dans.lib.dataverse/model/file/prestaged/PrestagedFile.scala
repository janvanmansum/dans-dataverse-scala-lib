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
package nl.knaw.dans.lib.dataverse.model.file.prestaged

/**
 * Metadata about a pre-staged file, which can be added to a dataset or replace an existing file in a dataset.
 *
 * @see [[https://guides.dataverse.org/en/latest/developers/s3-direct-upload-api.html]]
 * @param storageIdentifier the storage identifier of the pre-staged file
 * @param fileName          the file name
 * @param mimeType          the MIME type
 * @param checksum          the checksum
 * @param description       an optional description
 * @param directoryLabel    an optional directoryLabel
 * @param categories        the categories for this file
 * @param restrict          if `true` make the file restricted
 */
case class PrestagedFile(storageIdentifier: String,
                         fileName: String,
                         mimeType: String,
                         checksum: Checksum,
                         description: Option[String] = None,
                         directoryLabel: Option[String] = None,
                         categories: List[String] = List.empty,
                         restrict: Boolean = false,
                         forceReplace: Boolean = false)
