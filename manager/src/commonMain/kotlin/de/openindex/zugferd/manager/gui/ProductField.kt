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
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.model.Product
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.manager.utils.translate
import org.jetbrains.compose.resources.Resource

@Composable
fun ProductField(
    label: Resource,
    product: Product? = null,
    products: List<Product>,
    requiredIndicator: Boolean = false,
    onSelect: (Product?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferences = LocalAppState.current.preferences

    AutoCompleteField(
        label = label.translate(),
        entry = product,
        entries = remember(products, preferences.currency) {
            buildMap {
                products.forEach { product ->
                    put(product, product.getSummary(preferences))
                }
            }
        },
        requiredIndicator = requiredIndicator,
        onSelect = onSelect,
        modifier = modifier,
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ProductFieldWithAdd(
    label: Resource,
    addLabel: Resource,
    editLabel: Resource,
    product: Product? = null,
    products: List<Product>,
    requiredIndicator: Boolean = false,
    onSelect: (product: Product?, savePermanently: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferences = LocalAppState.current.preferences
    val defaultTaxPercentage = preferences.vatPercentage ?: 0.toDouble()

    var newProduct by remember { mutableStateOf<Product?>(null) }
    val unsavedProduct by derivedStateOf {
        products.find { !it.isSaved }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        ProductField(
            label = label,
            product = product,
            products = products,
            requiredIndicator = requiredIndicator,
            onSelect = { selection ->
                onSelect(selection, false)
            },
            modifier = Modifier.weight(1f, fill = true),
        )

        Tooltip(
            text = (addLabel.takeIf { unsavedProduct == null } ?: editLabel)
                .translate()
                .title(),
        ) {
            IconButton(
                onClick = {
                    newProduct = unsavedProduct ?: Product(
                        vatPercent = defaultTaxPercentage,
                    )
                },
                modifier = Modifier,
            ) {
                if (unsavedProduct == null) {
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

    if (newProduct != null) {
        ProductDialog(
            title = (addLabel.takeIf { unsavedProduct == null } ?: editLabel),
            value = newProduct!!,
            permanentSaveOption = true,
            onDismissRequest = { newProduct = null },
            onSubmitRequest = { selection, savePermanently ->
                onSelect(selection, savePermanently)
                newProduct = null
            },
        )
    }
}
