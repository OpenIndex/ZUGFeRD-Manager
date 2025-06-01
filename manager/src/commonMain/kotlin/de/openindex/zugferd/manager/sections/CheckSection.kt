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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.gui.ActionButtonWithTooltip
import de.openindex.zugferd.manager.gui.Label
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.SectionSubTitle
import de.openindex.zugferd.manager.gui.SectionTitle
import de.openindex.zugferd.manager.gui.Tooltip
import de.openindex.zugferd.manager.gui.VerticalScrollBox
import de.openindex.zugferd.manager.gui.WebViewer
import de.openindex.zugferd.manager.gui.XmlViewer
import de.openindex.zugferd.manager.model.ValidationSeverity
import de.openindex.zugferd.manager.model.ValidationType
import de.openindex.zugferd.manager.utils.ValidationMessage
import de.openindex.zugferd.manager.utils.createDragAndDropTarget
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.putText
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheck
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckDetailsHtml
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckDetailsPdf
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckDetailsXml
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckFailed
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessageMessageCopy
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessages
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessagesFilter
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessagesFilterSeverity
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckMessagesFilterType
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckPassed
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSelect
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSelectInfo
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSelectMessage
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummary
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryErrors
import de.openindex.zugferd.zugferd_manager.generated.resources.AppCheckSummaryExport
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
@OptIn(ExperimentalFoundationApi::class)
fun CheckSection(state: CheckSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current
    val selectedPdf = state.selectedPdf

    val dragAndDropCallback = remember {
        createDragAndDropTarget(
            onDrop = { pdfFile ->
                scope.launch {
                    state.selectPdf(
                        pdf = pdfFile,
                        appState = appState,
                    )
                }
            }
        )
    }

    // Show an empty view, if no PDF file was selected.
    if (selectedPdf == null) {
        EmptyView(state)
    }

    // Show two column layout, if a PDF file was selected.
    else {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                target = dragAndDropCallback,
                shouldStartDragAndDrop = { true },
            ),
    )
}

/**
 * Action buttons of the check section, shown on the top right.
 */
@Composable
fun CheckSectionActions(state: CheckSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current

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
                        appState = appState,
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
                textAlign = TextAlign.Center,
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
    val scope = rememberCoroutineScope()
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
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                state.exportValidation(validation)
                            }
                        },
                    ) {
                        Label(
                            text = Res.string.AppCheckSummaryExport,
                        )
                    }
                }

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
                    ) {
                        Box {
                            var expanded by remember { mutableStateOf(false) }

                            Button(
                                onClick = { expanded = true },
                            ) {
                                Label(
                                    text = Res.string.AppCheckMessagesFilter,
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
                                Text(
                                    text = stringResource(Res.string.AppCheckMessagesFilterType),
                                    style = MaterialTheme.typography.labelMedium,
                                    softWrap = false,
                                    modifier = Modifier
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 12.dp,
                                        )
                                )

                                ValidationType.entries
                                    .filter { type ->
                                        validation.messages.firstOrNull {
                                            it.type == type
                                        } != null
                                    }
                                    .forEach { type ->
                                        val isSelected = state.filterType.contains(type)

                                        DropdownMenuItem(
                                            text = {
                                                Label(
                                                    text = stringResource(type.title),
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isSelected)
                                                        Icons.Default.CheckBox
                                                    else
                                                        Icons.Default.CheckBoxOutlineBlank,
                                                    contentDescription = stringResource(type.title),
                                                )
                                            },
                                            onClick = {
                                                state.setFilterType(
                                                    if (state.filterType.contains(type))
                                                        state.filterType.minus(type)
                                                    else
                                                        state.filterType.plus(type)
                                                )
                                            }
                                        )
                                    }

                                HorizontalDivider()

                                Text(
                                    text = stringResource(Res.string.AppCheckMessagesFilterSeverity),
                                    style = MaterialTheme.typography.labelMedium,
                                    softWrap = false,
                                    modifier = Modifier
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 12.dp,
                                        )
                                )

                                ValidationSeverity.entries
                                    .filter { severity ->
                                        validation.messages.firstOrNull {
                                            it.severity == severity
                                        } != null
                                    }
                                    .forEach { severity ->
                                        val isSelected = state.filterSeverity.contains(severity)

                                        DropdownMenuItem(
                                            text = {
                                                Label(
                                                    text = stringResource(severity.title),
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isSelected)
                                                        Icons.Default.CheckBox
                                                    else
                                                        Icons.Default.CheckBoxOutlineBlank,
                                                    contentDescription = stringResource(severity.title),
                                                )
                                            },
                                            onClick = {
                                                state.setFilterSeverity(
                                                    if (state.filterSeverity.contains(severity))
                                                        state.filterSeverity.minus(severity)
                                                    else
                                                        state.filterSeverity.plus(severity)
                                                )
                                            }
                                        )
                                    }
                            }
                        }
                    }

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
    val filterType = state.filterType
    val filterSeverity = state.filterSeverity

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        validation.messages
            .filter { filterType.contains(it.type) }
            .filter { filterSeverity.contains(it.severity) }
            .forEach {
                ValidationMessage(it)
            }
    }
}

/**
 * A single validation message.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ValidationMessage(message: ValidationMessage) =
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
            ) {
                val scope = rememberCoroutineScope()
                val isDarkMode = LocalAppState.current.preferences.darkMode ?: isSystemInDarkTheme()
                val clipboard: Clipboard = LocalClipboard.current

                Icon(
                    imageVector = message.severity.icon,
                    tint = if (isDarkMode)
                        message.severity.darkModeColor
                    else
                        message.severity.lightModeColor,
                    contentDescription = stringResource(message.severity.title),
                    modifier = Modifier
                        .size(36.dp)
                )

                Text(
                    text = buildString {
                        append(stringResource(message.severity.title).title())
                        append(" (")
                        append(stringResource(message.type.title))
                        append(")")
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                        .copy(lineHeight = 1.em),
                )

                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f, fill = true),
                )

                Tooltip(
                    text = stringResource(Res.string.AppCheckMessageMessageCopy),
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                clipboard.putText(
                                    text = message.message,
                                )
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(Res.string.AppCheckMessageMessageCopy),
                        )
                    }
                }
            }

            Text(
                text = message.message.trim(),
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp),
            )
        }
    }
