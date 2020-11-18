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
package nl.knaw.dans.lib.dataverse.model.dataset

import scala.collection.mutable

case class CompoundFieldBuilder(id: String, multipleValues: Boolean = true) {
  private type FieldId = String

  // TODO: make this a Map[FieldId, MDF] as well
  // TODO: constants for keys such as "multiple", etc
  private val fields = mutable.ListBuffer[(FieldId, MetadataField)]()
  private val values = mutable.ListBuffer[Map[FieldId, MetadataField]]()

  def withSingleValueField(fieldId: FieldId, value: String): CompoundFieldBuilder = {
    fields.append((fieldId, MetadataField(typeName = fieldId, multiple = false, typeClass = "primitive", value = value)))
    this
  }

  def withMultiValueField(fieldId: FieldId, values: List[String]): CompoundFieldBuilder = {
    fields.append((fieldId, MetadataField(typeName = fieldId, multiple = true, typeClass = "primitive", value = values)))
    this
  }

  def withControlledSingleValueField(fieldId: FieldId, value: String): CompoundFieldBuilder = {
    fields.append((fieldId, MetadataField(typeName = fieldId, multiple = false, typeClass = "controlledVocabulary", value = value)))
    this
  }

  def withControlledMultiValueField(fieldId: FieldId, values: List[String]): CompoundFieldBuilder = {
    fields.append((fieldId, MetadataField(typeName = fieldId, multiple = true, typeClass = "controlledVocabulary", value = values)))
    this
  }

  def addValue(): CompoundFieldBuilder = {
    values.append(fields.toMap)
    fields.clear()
    this
  }

  def build(): MetadataField = {
    if (fields.nonEmpty) addValue()
    if (!multipleValues && values.size > 1) throw new IllegalStateException("Single-value field with more than one value")
    MetadataField(typeName = id, multiple = multipleValues, typeClass = "compound", value = values.toList)
  }
}
