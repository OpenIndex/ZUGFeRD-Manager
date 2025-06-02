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

expect fun Number.format(
    minPrecision: Int = 0,
    maxPrecision: Int = 2,
    grouped: Boolean = false,
): String

expect fun Number.formatPrice(
    currencyCode: String,
    grouped: Boolean = false,
): String

val Number.formatAsPercentage: String
    get() = format(
        minPrecision = 0,
        //minPrecision = 1,
        maxPrecision = 1,
        grouped = false,
    )

@Suppress("unused")
val Number.formatAsQuantity: String
    get() = format(
        minPrecision = 1,
        maxPrecision = 2,
        grouped = false,
    )

expect fun String.parseNumber(
    grouped: Boolean = false,
): Number?

fun String.parseDouble(
    grouped: Boolean = false,
): Double? = parseNumber(
    grouped = grouped,
)?.toDouble()

fun String.parseFloat(
    grouped: Boolean = false,
): Float? = parseNumber(
    grouped = grouped,
)?.toFloat()

fun String.parseLong(
    grouped: Boolean = false,
): Long? = parseNumber(
    grouped = grouped,
)?.toLong()

fun String.parseInt(
    grouped: Boolean = false,
): Int? = parseNumber(
    grouped = grouped,
)?.toInt()
