/**
 * Copyright (C) 2021 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
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
package nl.knaw.dans.easydv.subcommand

import nl.knaw.dans.easydv.subCommandFooter
import org.rogach.scallop.Subcommand

class SwordSubcommand extends Subcommand("sword") {
  shortSubcommandsHelp(true)
  descr("Operations via the SWORD API. See: https://guides.dataverse.org/en/latest/api/sword.html")

  footer(subCommandFooter)
}
