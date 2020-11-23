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

import java.io.FileInputStream
import java.net.URI
import java.nio.charset.StandardCharsets

import better.files.File
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import scalaj.http.{ Http, MultiPart }

import scala.util.Try

private[dataverse] trait HttpSupport extends DebugEnhancedLogging {
  private val HEADER_CONTENT_TYPE = "Content-Type"
  private val HEADER_X_DATAVERSE_KEY = "X-Dataverse-key"

  private val MEDIA_TYPE_JSON = "application/json"
  private val MEDIA_TYPE_OCTET_STREAM = "application/octet-stream"
  private val MEDIA_TYPE_TEXT = "text/plain"

  private val METHOD_GET = "GET"
  private val METHOD_POST = "POST"
  private val METHOD_PUT = "PUT"

  protected val connectionTimeout: Int
  protected val readTimeout: Int
  protected val baseUrl: URI
  protected val apiToken: String
  protected val apiVersion: String

  protected def postFile[D: Manifest](subPath: String, file: File, optJsonMetadata: Option[String] = None): Try[DataverseResponse[D]] = {
    trace(subPath, file, optJsonMetadata)
    for {
      uri <- createUri(Option(subPath))
      response <- httpPostMulti[D](uri, file, optJsonMetadata, Map(HEADER_X_DATAVERSE_KEY -> apiToken))
    } yield response
  }

  private def httpPostMulti[D: Manifest](uri: URI, file: File, optJsonMetadata: Option[String] = None, headers: Map[String, String] = Map()): Try[DataverseResponse[D]] = Try {
    trace(uri, file, optJsonMetadata, headers)
    val parts = MultiPart(name = "file", filename = file.name, mime = MEDIA_TYPE_OCTET_STREAM, new FileInputStream(file.pathAsString), file.size, lenWritten => {}) +:
      optJsonMetadata.map {
        json => List(MultiPart(data = json.getBytes(StandardCharsets.UTF_8), name = "jsonData", filename = "jsonData", mime = MEDIA_TYPE_JSON))
      }.getOrElse(Nil)

    val response = Http(uri.toASCIIString).postMulti(parts: _*)
      .timeout(connTimeoutMs = connectionTimeout, readTimeoutMs = readTimeout)
      .headers(headers)
      .asBytes

    if (response.code >= 200 && response.code < 300) DataverseResponse(response)
    else throw DataverseException(response.code, new String(response.body, StandardCharsets.UTF_8), response)
  }

  protected def get[D: Manifest](subPath: String = null): Try[DataverseResponse[D]] = {
    trace(subPath)
    for {
      uri <- createUri(Option(subPath))
      response <- http[D](METHOD_GET, uri, body = null, Map(HEADER_X_DATAVERSE_KEY -> apiToken))
    } yield response
  }

  protected def postJson[D: Manifest](subPath: String = null)(body: String = null): Try[DataverseResponse[D]] = {
    trace(subPath, body)
    for {
      uri <- createUri(Option(subPath))
      response <- http[D](METHOD_POST, uri, body, Map(HEADER_CONTENT_TYPE -> MEDIA_TYPE_JSON, HEADER_X_DATAVERSE_KEY -> apiToken))
    } yield response
  }

  protected def postText[D: Manifest](subPath: String = null)(body: String = null): Try[DataverseResponse[D]] = {
    for {
      uri <- createUri(Option(subPath))
      response <- http[D](METHOD_POST, uri, body, Map(HEADER_CONTENT_TYPE -> MEDIA_TYPE_TEXT, HEADER_X_DATAVERSE_KEY -> apiToken))
    } yield response
  }

  protected def put[D: Manifest](subPath: String = null)(body: String = null): Try[DataverseResponse[D]] = {
    for {
      uri <- createUri(Option(subPath))
      response <- http[D](METHOD_PUT, uri, body, Map(HEADER_X_DATAVERSE_KEY -> apiToken))
    } yield response
  }

  protected def deletePath[D: Manifest](subPath: String = null): Try[DataverseResponse[D]] = {
    for {
      uri <- createUri(Option(subPath))
      response <- http[D]("DELETE", uri, null, Map(HEADER_X_DATAVERSE_KEY -> apiToken))
    } yield response
  }

  private def createUri(subPath: Option[String]): Try[URI] = Try {
    baseUrl resolve new URI(s"api/v${ apiVersion }/${ subPath.getOrElse("") }")
  }

  private def http[D: Manifest](method: String, uri: URI, body: String = null, headers: Map[String, String] = Map.empty[String, String]): Try[DataverseResponse[D]] = Try {
    trace(method, uri, body, headers)
    debug(s"Request URL = $uri")
    val response = {
      if (body == null) Http(uri.toASCIIString)
      else Http(uri.toASCIIString).postData(body)
    }.method(method)
      .headers(headers)
      .timeout(connTimeoutMs = connectionTimeout, readTimeoutMs = readTimeout)
      .asBytes
    if (response.code >= 200 && response.code < 300) new DataverseResponse(response)
    else throw DataverseException(response.code, new String(response.body, StandardCharsets.UTF_8), response)
  }
}