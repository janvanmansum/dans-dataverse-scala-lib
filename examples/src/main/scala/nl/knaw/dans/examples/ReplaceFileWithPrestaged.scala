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
package nl.knaw.dans.examples

import nl.knaw.dans.lib.dataverse.model.file.prestaged.Checksum
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats }

/**
 * Before executing this code you must create the replacement file at
 *
 * s3://bucket-name/DOI-of-dataset/storage-identifier
 *
 * with appropriate values for bucket-name, DOI-of-dataset and storage-identifier.
 *
 * Not tested for file storage, but that should work similarly.
 *
 */
object ReplaceFileWithPrestaged extends App with DebugEnhancedLogging with BaseApp {
  private implicit val jsonFormats: Formats = DefaultFormats
  private val doi = args(0)
  private val databaseId = args(1).toInt
  private val storageId = args(2)
  private val sha1Checksum = args(3) // Checksum of the replacement
  private val fileSize = args(4).toInt // Length of the replacement

  val result = for {
    response <- server.dataset(doi).listFiles()
    fileMetas <- response.data
    dataFile = fileMetas.map(_.dataFile).find(_.exists(_.id == databaseId)).flatten.get
    newDataFile = dataFile.toPrestaged.copy(storageIdentifier = storageId, checksum = Checksum(`@type` = "SHA-1", `@value` = sha1Checksum), fileSize = fileSize)
    response <- server.file(databaseId).replaceWithPrestagedFile(newDataFile)
    _ = logger.info(s"Raw response message: ${ response.string }")
    _ = logger.info(s"JSON AST: ${ response.json }")
    _ = logger.info(s"JSON serialized: ${ Serialization.writePretty(response.json) }")
    fileList <- response.data
    _ = logger.info(s"File has ${ fileList.files.head.dataFile.get.checksum.`type` } checksum ${ fileList.files.head.dataFile.get.checksum.value }")
  } yield ()
  logger.info(s"result = $result")
}
