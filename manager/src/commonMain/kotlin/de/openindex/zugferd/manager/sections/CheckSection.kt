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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.openindex.zugferd.manager.gui.ActionButtonWithTooltip
import de.openindex.zugferd.manager.gui.Label
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.SectionSubTitle
import de.openindex.zugferd.manager.gui.SectionTitle
import de.openindex.zugferd.manager.gui.VerticalScrollBox
import de.openindex.zugferd.manager.gui.WebViewer
import de.openindex.zugferd.manager.gui.XmlViewer
import de.openindex.zugferd.manager.utils.LocalPreferences
import de.openindex.zugferd.manager.utils.ValidationMessage
import de.openindex.zugferd.manager.utils.ValidationSeverity
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheck
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckDetailsHtml
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckDetailsPdf
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckDetailsXml
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckFailed
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessages
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckPassed
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSelect
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSelectInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSelectMessage
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummary
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryErrors
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryMessages
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryNotices
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryProfile
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummarySignature
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryUnknown
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryVersion
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryWarnings
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main view of the check section.
 */
@Composable
fun CheckSection(state: CheckSectionState) {
    val selectedPdf = state.selectedPdf

    // Show an empty view, if no PDF file was selected.
    if (selectedPdf == null) {
        EmptyView(state)
        return
    }

    Row(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // Left column with e-invoice validation.
        Column(
            modifier = Modifier
                .weight(0.6f, fill = true),
        ) {
            // Validation result.
            VerticalScrollBox(
                modifier = Modifier
                    .weight(1f, fill = true),
            ) {
                CheckView(state)
            }
        }

        // Right column with further details about the selected PDF.
        Column(
            modifier = Modifier
                .weight(0.4f, fill = true),
        ) {
            DetailsView(state)
        }
    }
}

/**
 * Action buttons of the check section, shown on the top right.
 */
@Composable
fun CheckSectionActions(state: CheckSectionState) {
    val scope = rememberCoroutineScope()
    val preferences = LocalPreferences.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(end = 8.dp),
    ) {
        // Add button to select a PDF file for validation.
        ActionButtonWithTooltip(
            label = Res.string.AppCheckSelect,
            tooltip = Res.string.AppCheckSelectInfo,
            onClick = {
                scope.launch(Dispatchers.IO) {
                    state.selectPdf(
                        preferences = preferences,
                    )
                }
            },
        )
    }
}

/**
 * Empty view of the check section.
 * This is shown, if no PDF file was selected by the user.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun EmptyView(state: CheckSectionState) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize(),
        ) {
            // Request user to select a PDF file.
            Text(
                text = stringResource(Res.string.AppCheckSelectMessage),
                softWrap = true,
            )
        }
    }
}

/**
 * Left side view of the check section.
 * This provides the validation view.
 */
@Composable
private fun CheckView(state: CheckSectionState) {
    val selectedPdf = state.selectedPdf!!
    val validation = state.selectedPdfValidation

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        // Section title with validation icon.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            AnimatedVisibility(visible = validation != null) {
                Icon(
                    imageVector = Icons.Default.ThumbUp
                        .takeIf { validation?.isValid == true }
                        ?: Icons.Default.ThumbDown,
                    contentDescription = stringResource(
                        Res.string.AppCheckPassed
                            .takeIf { validation?.isValid == true }
                            ?: Res.string.AppCheckFailed,
                        selectedPdf.name,
                    ),
                    modifier = Modifier
                        .size(32.dp)
                )
            }

            SectionTitle(
                text = when (validation?.isValid) {
                    true -> stringResource(Res.string.AppCheckPassed, selectedPdf.name)
                    false -> stringResource(Res.string.AppCheckFailed, selectedPdf.name)
                    else -> stringResource(Res.string.AppCheck, selectedPdf.name)
                },
                modifier = Modifier
                    .weight(1f, fill = true),
            )
        }

        // Validation result.
        if (validation == null) {
            CircularProgressIndicator()
        } else {
            // Subsection with validation summary.
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SectionSubTitle(
                    text = Res.string.AppCheckSummary,
                )

                ValidationSummary(state)
            }

            // Subsection with validation messages.
            if (validation.messages.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    SectionSubTitle(
                        text = Res.string.AppCheckMessages,
                    )

                    ValidationMessages(state)
                }
            }
        }
    }

    /*TextField(
        value = JSON_EXPORT.encodeToString(validation),
        readOnly = true,
        singleLine = false,
        onValueChange = {},
        modifier = Modifier.fillMaxSize(),
    )*/
}

/**
 * Right side view of the check section.
 * This provides the PDF- / HTML- / XML-viewer.
 */
@Composable
private fun DetailsView(state: CheckSectionState) {
    val selectedPdf = state.selectedPdf
    var tabState by remember { mutableStateOf(0) }
    val isPdfTabSelected by derivedStateOf { tabState == 0 }
    val isHtmlTabSelected by derivedStateOf { tabState == 1 && state.selectedPdfHtml != null }
    val isXmlTabSelected by derivedStateOf { tabState == 2 && state.selectedPdfXml != null }

    TabRow(
        selectedTabIndex = tabState,
    ) {
        // Add tab for PDF viewer.
        Tab(
            selected = isPdfTabSelected,
            onClick = { tabState = 0 },
            text = {
                Label(
                    text = Res.string.AppCheckDetailsPdf,
                )
            },
        )

        // Add tab for HTML viewer.
        if (state.selectedPdfHtml != null) {
            Tab(
                selected = isHtmlTabSelected,
                onClick = { tabState = 1 },
                text = {
                    Label(
                        text = Res.string.AppCheckDetailsHtml,
                    )
                },
            )
        }

        // Add tab for XML viewer.
        if (state.selectedPdfXml != null) {
            Tab(
                selected = isXmlTabSelected,
                onClick = { tabState = 2 },
                text = {
                    Label(
                        text = Res.string.AppCheckDetailsXml,
                    )
                },
            )
        }
    }

    // Show PDF viewer.
    if (isPdfTabSelected) {
        PdfViewer(
            pdf = selectedPdf!!,
            modifier = Modifier.fillMaxSize(),
        )
    }

    // Show HTML viewer.
    if (isHtmlTabSelected) {
        WebViewer(
            html = state.selectedPdfHtml ?: "",
            modifier = Modifier.fillMaxSize(),
        )
    }

    // Show XML viewer.
    if (isXmlTabSelected) {
        XmlViewer(
            xml = state.selectedPdfXml ?: "",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Summary about the validation.
 */
@Composable
private fun ValidationSummary(state: CheckSectionState) {
    val validation = state.selectedPdfValidation!!

    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),
        ) {
            // Invoice details on the left side.
            Text(
                text = buildAnnotatedString {
                    val bold = SpanStyle(fontWeight = FontWeight.Bold)
                    val unknown = SpanStyle(fontStyle = FontStyle.Normal)

                    // Invoice profile.
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCheckSummaryProfile).title())
                        append("\n")
                    }
                    withStyle(style = bold.takeIf { validation.profile != null } ?: unknown) {
                        append(
                            validation.profile?.split(":")?.joinToString("\n")
                                ?: stringResource(Res.string.AppCheckSummaryUnknown)
                        )
                        append("\n")
                    }

                    // Invoice version.
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCheckSummaryVersion).title())
                        append("\n")
                    }
                    withStyle(style = bold.takeIf { validation.version != null } ?: unknown) {
                        append(
                            validation.version
                                ?: stringResource(Res.string.AppCheckSummaryUnknown)
                        )
                        append("\n")
                    }

                    // Invoice signature.
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCheckSummarySignature).title())
                        append("\n")
                    }
                    withStyle(style = bold.takeIf { validation.signature != null } ?: unknown) {
                        append(
                            validation.signature
                                ?: stringResource(Res.string.AppCheckSummaryUnknown)
                        )
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            // Message count on the right side.
            Text(
                text = buildAnnotatedString {
                    // Title.
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCheckSummaryMessages).title())
                        append("\n")
                    }

                    // Total number of errors.
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            pluralStringResource(
                                Res.plurals.AppCheckSummaryErrors,
                                validation.countErrors,
                                validation.countErrors
                            )
                        )
                        append("\n")
                    }

                    // Total number of warnings.
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            pluralStringResource(
                                Res.plurals.AppCheckSummaryWarnings,
                                validation.countWarnings,
                                validation.countWarnings,
                            )
                        )
                        append("\n")
                    }

                    // Total number of notices.
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            pluralStringResource(
                                Res.plurals.AppCheckSummaryNotices,
                                validation.countNotices,
                                validation.countNotices
                            )
                        )
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                softWrap = false,
            )
        }
    }
}

/**
 * List of validation messages.
 */
@Composable
private fun ValidationMessages(state: CheckSectionState) {
    val validation = state.selectedPdfValidation!!

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        validation.messages.forEach {
            ValidationMessage(it)
        }
    }
}

/**
 * A single validation message.
 */
@Composable
private fun ValidationMessage(message: ValidationMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),
        ) {
            Icon(
                imageVector = when (message.severity) {
                    ValidationSeverity.FATAL, ValidationSeverity.ERROR -> Icons.Default.Error
                    ValidationSeverity.WARNING -> Icons.Default.Warning
                    ValidationSeverity.NOTICE -> Icons.Default.Info
                },
                contentDescription = "",
                modifier = Modifier
                    .size(36.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(message.severity.title).title())
                        append(" (")
                        append(stringResource(message.type.title))
                        append(")\n")
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(message.message.trim())
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
