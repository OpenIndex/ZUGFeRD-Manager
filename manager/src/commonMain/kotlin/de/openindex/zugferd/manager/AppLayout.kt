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

package de.openindex.zugferd.manager

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.zugferd_manager.generated.resources.Res
import de.openindex.zugferd.zugferd_manager.generated.resources.application
import org.jetbrains.compose.resources.painterResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppLayout(
) {
    val appState = LocalAppState.current
    //val blur: Float by animateFloatAsState(
    //    targetValue = if (appState.locked) 10f else 0f,
    //    animationSpec = tween(
    //        easing = FastOutLinearInEasing,
    //        durationMillis = 250,
    //    ),
    //    label = "blur",
    //)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier,
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.application),
                            contentDescription = APP_TITLE,
                            modifier = Modifier
                                .padding(start = 4.dp, end = 38.dp)
                                .width(40.dp)
                        )

                        Text(
                            text = APP_TITLE,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                },
                actions = {
                    appState.section.actions()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier,
            )
        },
        //bottomBar = {},
        //snackbarHost = {},
        //floatingActionButton = {},
        //floatingActionButtonPosition = FabPosition.End,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = contentColorFor(MaterialTheme.colorScheme.background),
        //contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        //modifier = Modifier,
        //modifier = appState.lockedModifier,
        modifier = Modifier
        //.blur(blur.dp),
    ) { innerPadding ->
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            AppNavigation()
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    //DummyContent(
    //    modifier = Modifier
    //        .background(color = Color.LightGray)
    //        .fillMaxWidth(),
    //)

    //VerticalScrollBox {
    //    Surface(
    //        modifier = Modifier.fillMaxSize(),
    //    ) {
    //        Column {
    //            for (item in 0..30) {
    //                Text(text = "Item #$item")
    //                if (item < 30) {
    //                    Spacer(modifier = Modifier.height(5.dp))
    //                }
    //            }
    //        }
    //    }
    //}

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        LocalAppState.current.section.content()
    }

    /*VerticalScrollBox {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            LocalAppState.current.section.content()
        }
    }*/
}

@Composable
private fun AppNavigation() {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        //windowInsets = NavigationRailDefaults.windowInsets,
        //header = { Text(text = "Head") },
        modifier = Modifier,
    ) {
        Spacer(
            modifier = Modifier
                .height(4.dp),
        )

        //Text(text = "Item")
        AppSection.entries.forEach {
            AppNavigationItem(section = it)
        }

        Spacer(
            modifier = Modifier
                .weight(1f, fill = true),
        )

        Text(
            text = "v${APP_VERSION_SHORT}",
            softWrap = false,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(all = 8.dp),
        )
    }
}

@Composable
private fun AppNavigationItem(section: AppSection) {
    val appState = LocalAppState.current

    NavigationRailItem(
        selected = appState.isSection(section),
        onClick = { appState.setSection(section) },
        icon = {
            Icon(
                imageVector = if (appState.isSection(section)) section.activeIcon else section.inactiveIcon,
                contentDescription = section.label,
            )
        },
        enabled = true,
        label = { Text(text = section.label) },
        alwaysShowLabel = true,
        //colors = NavigationRailItemDefaults.colors(),
        colors = NavigationRailItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        //interactionSource = null,
        modifier = Modifier,
    )
}
