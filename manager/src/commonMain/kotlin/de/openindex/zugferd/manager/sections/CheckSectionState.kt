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

package de.openindex.zugferd.manager.sections

import androidx.compose.runtime.mutableStateOf
import de.openindex.zugferd.manager.AppState
import de.openindex.zugferd.manager.model.ValidationSeverity
import de.openindex.zugferd.manager.model.ValidationType
import de.openindex.zugferd.manager.utils.InvoiceFileFormat
import de.openindex.zugferd.manager.utils.SectionState
import de.openindex.zugferd.manager.utils.Validation
import de.openindex.zugferd.manager.utils.getPrettyPrintedXml
import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.manager.utils.getXmlFromPdf
import de.openindex.zugferd.manager.utils.readAsString
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.manager.utils.trimToNull
import de.openindex.zugferd.manager.utils.validateInvoiceFile
import de.openindex.zugferd.manager.utils.visualizeInvoiceXml
import de.openindex.zugferd.manager.utils.writeJson
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSelectFile
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile

class CheckSectionState : SectionState() {
    private var _invoiceFile = mutableStateOf<PlatformFile?>(null)
    val invoiceFile: PlatformFile?
        get() = _invoiceFile.value
    val invoiceFileFormat: InvoiceFileFormat?
        get() = if (isPdfInvoice) InvoiceFileFormat.PDF
        else if (isXmlInvoice) InvoiceFileFormat.XML
        else null
    val isPdfInvoice: Boolean
        get() = invoiceFile?.name?.endsWith(".pdf", true) == true
    val isXmlInvoice: Boolean
        get() = invoiceFile?.name?.endsWith(".xml", true) == true

    private var _invoiceXml = mutableStateOf<String?>(null)
    val invoiceXml: String?
        get() = _invoiceXml.value

    private var _invoiceHtml = mutableStateOf<String?>(null)
    val invoiceHtml: String?
        get() = _invoiceHtml.value

    private var _invoiceValidation = mutableStateOf<Validation?>(null)
    val invoiceValidation: Validation?
        get() = _invoiceValidation.value

    suspend fun selectInvoice(appState: AppState) {
        val invoiceFile = FileKit.pickFile(
            type = PickerType.File(extensions = listOf("pdf", "xml")),
            mode = PickerMode.Single,
            title = getString(Res.string.AppCheckSelectFile).title(),
            initialDirectory = appState.preferences.previousPdfLocation,
        ) ?: return

        selectInvoice(
            invoiceFile = invoiceFile,
            appState = appState,
        )
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun selectInvoice(invoiceFile: PlatformFile, appState: AppState) {
        _invoiceFile.value = invoiceFile
        _invoiceHtml.value = null
        _invoiceXml.value = null
        _invoiceValidation.value = null

        if (invoiceFileFormat == null) {
            _invoiceFile.value = null
            throw IllegalArgumentException("Invoice file is neither PDF nor XML!")
        }

        _invoiceXml.value = when (invoiceFileFormat) {
            InvoiceFileFormat.PDF -> getXmlFromPdf(invoiceFile)
            InvoiceFileFormat.XML -> invoiceFile.readAsString()
            else -> null
        }
            ?.let { getPrettyPrintedXml(it) }
            ?.trimToNull()

        _invoiceXml.value?.let { xml ->
            _invoiceHtml.value = visualizeInvoiceXml(xml)
        }
        _invoiceValidation.value = validateInvoiceFile(invoiceFile)
        _filterType.value = ValidationType.entries.toList()
        _filterSeverity.value = ValidationSeverity.entries.toList()
    }

    suspend fun exportValidation(validation: Validation) {
        val sourceFile = invoiceFile ?: return
        val targetFile = FileKit.saveFile(
            bytes = null,
            baseName = sourceFile.name.substringBeforeLast(".")
                .plus(".validation"),
            extension = "json",
            initialDirectory = sourceFile.path,
        ) ?: return

        targetFile.writeJson(validation)
    }

    private var _filterType = mutableStateOf(ValidationType.entries.toList())
    val filterType: List<ValidationType>
        get() = _filterType.value

    fun setFilterType(type: List<ValidationType>) {
        _filterType.value = type.toList()
    }

    private var _filterSeverity = mutableStateOf(ValidationSeverity.entries.toList())
    val filterSeverity: List<ValidationSeverity>
        get() = _filterSeverity.value

    fun setFilterSeverity(severity: List<ValidationSeverity>) {
        _filterSeverity.value = severity.toList()
    }
}
