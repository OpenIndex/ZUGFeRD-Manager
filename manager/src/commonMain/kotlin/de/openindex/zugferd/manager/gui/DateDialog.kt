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

package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.LocalAppState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DateDialog(
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appState = LocalAppState.current
    appState.setLocked(true)

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = value
            ?.atStartOfDayIn(TimeZone.currentSystemDefault())
            ?.toEpochMilliseconds()
    )

    var initialRender by remember { mutableStateOf(true) }

    LaunchedEffect(dateState.selectedDateMillis) {
        if (initialRender) {
            initialRender = false
            return@LaunchedEffect
        }

        val millis = dateState.selectedDateMillis
        if (millis == null) {
            onValueChange(null)
        } else {
            onValueChange(
                Instant
                    .fromEpochMilliseconds(millis)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            )
        }
        appState.setLocked(false)
    }

    DialogWindow(
        onCloseRequest = {
            onDismiss()
            appState.setLocked(false)
        },
        title = "Datum wählen",
        width = 400.dp,
        height = 550.dp,
        resizable = false,
    ) {
        DatePicker(
            state = dateState,
            showModeToggle = true,
            modifier = Modifier
                .fillMaxSize(),
        )
    }

    /*
    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    val millis = dateState.selectedDateMillis
                    if (millis == null) {
                        onValueChange(null)
                    } else {
                        onValueChange(
                            Instant
                                .fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                        )
                    }
                }
            ) {
                Text(
                    text = "Übernehmen",
                    softWrap = false,
                )
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text(
                    text = "Abbrechen",
                    softWrap = false,
                )
            }
        },
        modifier = modifier,
    ) {
        DatePicker(
            state = dateState,
            showModeToggle = true,
        )
    }
    */
}
