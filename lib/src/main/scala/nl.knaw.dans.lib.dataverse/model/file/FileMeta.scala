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
package nl.knaw.dans.lib.dataverse.model.file

import nl.knaw.dans.lib.dataverse.model.file.prestaged.PrestagedFile

case class FileMeta(label: Option[String] = None,
                    description: Option[String] = None,
                    directoryLabel: Option[String] = None,
                    restrict: Option[Boolean] = None,
                    categories: List[String] = List.empty[String],
                    dataFile: Option[DataFile] = None,
                    forceReplace: Boolean = false) {

  /**
   * Converts this FileMeta to a PrestagedFile object
   *
   * @return a PrestagedFile object
   * @throws IllegalArgumentException if the FileMeta has no dataFile field
   */
  def toPrestaged: PrestagedFile = {
    if (dataFile.isEmpty) throw new IllegalArgumentException("FileMeta has no dataFile, cannot convert to PrestagedFile")
    else {
      val df = dataFile.get
      PrestagedFile(
        storageIdentifier = df.storageIdentifier,
        fileName = label.get,
        mimeType = df.contentType,
        checksum = prestaged.Checksum(
          `@type` = df.checksum.`type`,
          `@value` = df.checksum.value
        ),
        description = description,
        directoryLabel = directoryLabel,
        categories = categories,
        restrict = restrict.getOrElse(false),
        forceReplace = forceReplace
      )
    }
  }
}
