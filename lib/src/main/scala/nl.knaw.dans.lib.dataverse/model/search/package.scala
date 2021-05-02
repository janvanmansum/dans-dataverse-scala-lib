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
package nl.knaw.dans.lib.dataverse.model

import nl.knaw.dans.lib.dataverse.model.dataset.{ CompoundField, ControlledMultipleValueField, ControlledSingleValueField, MetadataField, PrimitiveMultipleValueField, PrimitiveSingleValueField, TYPE_CLASS_COMPOUND, TYPE_CLASS_CONTROLLED_VOCABULARY, TYPE_CLASS_PRIMITIVE }
import org.json4s.{ CustomSerializer, DefaultFormats, Extraction, Formats, JNull, JObject }

package object search {
  val SEARCH_RESULT_TYPE_DATAVERSE = "dataverse"
  val SEARCH_RESULT_TYPE_DATASET = "dataset"
  val SEARCH_RESULT_TYPE_FILE = "file"

  implicit val jsonFormats: Formats = DefaultFormats

  object ResultItemSerializer extends CustomSerializer[ResultItem](_ => ( {
    case jsonObj: JObject =>
      val `type` = (jsonObj \ "type").extract[String]

      `type` match {
        case SEARCH_RESULT_TYPE_DATAVERSE => Extraction.extract[DataverseResultItem](jsonObj)
        case SEARCH_RESULT_TYPE_DATASET => Extraction.extract[DatasetResultItem](jsonObj)
        case SEARCH_RESULT_TYPE_FILE => Extraction.extract[FileResultItem](jsonObj)
      }
  }, {
    case null => JNull
  }
  ))
}
