/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mustangproject.ZUGFeRD.ZUGFeRDVisualizer
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString
import kotlin.io.path.writer

private val CUSTOM_VISUALIZATION_CSS = """
    body > form {
        position: fixed !important;
        left: 0 !important;
        right: 0 !important;
        z-index: 1 !important;
    }

    .inhalt {
        padding-top: 75px !important;    
    }
    
    .menue > .innen {
        text-align: center !important;
    }
    
    .menue > .innen > button {
        font-family: sans-serif !important;
        font-size: 14px !important;
    }
""".trimIndent()

actual suspend fun visualizeInvoiceXml(xml: String): String? {
    return withContext(Dispatchers.IO) {
        val tempXmlFile = Files.createTempFile("zugferd-", ".xml")
        tempXmlFile.writer().use { writer ->
            writer.write(xml)
        }

        try {
            ZUGFeRDVisualizer()
                .visualize(
                    tempXmlFile.pathString,
                    ZUGFeRDVisualizer.Language.DE,
                )
                // HACK: Apply custom css.
                .replace(
                    "</head>",
                    "\n<style>\n${CUSTOM_VISUALIZATION_CSS}</style>\n</head>"
                )

            //APP_LOGGER.debug("generated HTML\n${html}")
        } catch (e: Exception) {
            APP_LOGGER.error("Can't create HTML visualization.", e)
            null
        } finally {
            tempXmlFile.deleteIfExists()
        }
    }

    /*
    try {
        ExportResource("/xrechnung-viewer.css")
        ExportResource("/xrechnung-viewer.js")

        println("xrechnung-viewer.css and xrechnung-viewer.js written as well (to local working dir)")
    } catch (e: java.lang.Exception) {
        LOGGER.error(e.message, e)
    }
    */
}
