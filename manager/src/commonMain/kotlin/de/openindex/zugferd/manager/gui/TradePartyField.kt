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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.model.TradeParty
import de.openindex.zugferd.manager.utils.getDefaultCountryCode
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.manager.utils.translate
import org.jetbrains.compose.resources.Resource

@Composable
fun TradePartyField(
    label: Resource,
    tradeParty: TradeParty? = null,
    tradeParties: List<TradeParty>,
    requiredIndicator: Boolean = false,
    onSelect: (TradeParty?) -> Unit,
    modifier: Modifier = Modifier,
) = AutoCompleteField(
    label = label.translate(),
    entry = tradeParty,
    entries = remember(tradeParties) {
        buildMap {
            tradeParties.forEach { party ->
                put(party, party.summary)
            }
        }
    },
    requiredIndicator = requiredIndicator,
    onSelect = onSelect,
    modifier = modifier,
)

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TradePartyFieldWithAdd(
    label: Resource,
    addLabel: Resource,
    editLabel: Resource,
    tradeParty: TradeParty? = null,
    tradeParties: List<TradeParty>,
    requiredIndicator: Boolean = false,
    onSelect: (tradeParty: TradeParty?, savePermanently: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var newTradeParty by remember { mutableStateOf<TradeParty?>(null) }
    val unsavedTradeParty by derivedStateOf {
        tradeParties.find { !it.isSaved }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        TradePartyField(
            label = label,
            tradeParty = tradeParty,
            tradeParties = tradeParties,
            requiredIndicator = requiredIndicator,
            onSelect = { selection ->
                onSelect(selection, false)
            },
            modifier = Modifier.weight(1f, fill = true),
        )

        Tooltip(
            text = (addLabel.takeIf { unsavedTradeParty == null } ?: editLabel)
                .translate()
                .title(),
        ) {
            IconButton(
                onClick = {
                    newTradeParty = unsavedTradeParty ?: TradeParty(
                        country = getDefaultCountryCode(),
                    )
                },
                modifier = Modifier,
            ) {
                if (unsavedTradeParty == null) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = addLabel.translate(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = editLabel.translate(),
                    )
                }
            }
        }
    }

    if (newTradeParty != null) {
        TradePartyDialog(
            title = (addLabel.takeIf { unsavedTradeParty == null } ?: editLabel)
                .translate()
                .title(),
            value = newTradeParty!!,
            permanentSaveOption = true,
            onDismissRequest = { newTradeParty = null },
            onSubmitRequest = { selection, savePermanently ->
                onSelect(selection, savePermanently)
                newTradeParty = null
            },
        )
    }
}
