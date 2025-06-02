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

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NumberTest {
    private val language: Language = getCurrentLanguage()

    @BeforeTest
    fun onBefore() {
        setCurrentLanguage(Language.EN)
    }

    @AfterTest
    fun onAfter() {
        setCurrentLanguage(language)
    }

    @Test
    fun format() {
        assertEquals(
            "123",
            123.format(),
            "integer number format",
        )

        assertEquals(
            "1234",
            1234.format(),
            "integer number format without grouping",
        )

        assertEquals(
            "1234",
            1234.0.format(),
            "float number format with zero decimal places",
        )

        assertEquals(
            "1234.1",
            1234.10.format(),
            "float number format with reduced decimal places",
        )

        assertEquals(
            "1234.12",
            1234.12.format(),
            "float number format with two decimal places",
        )

        assertEquals(
            "1234.13",
            1234.125.format(),
            "float number format with three decimal places and rounding half up",
        )

        assertEquals(
            "1234.0",
            1234.format(
                minPrecision = 1,
            ),
            "integer number format with enforced minimal precision",
        )

        assertEquals(
            "1234.1",
            1234.1.format(
                minPrecision = 1,
            ),
            "float number format with minimal precision",
        )

        assertEquals(
            "1234.123",
            1234.1234.format(
                maxPrecision = 3,
            ),
            "float number format with maximal precision",
        )

        assertEquals(
            "1,234",
            1234.format(
                grouped = true,
            ),
            "integer number format with grouping",
        )

        assertEquals(
            "1,234,567",
            1234567.format(
                grouped = true,
            ),
            "integer number format with grouping",
        )
    }

    @Test
    fun formatPrice() {
        assertEquals(
            "€123.00",
            123.formatPrice(
                currencyCode = FALLBACK_CURRENCY,
            ),
            "integer price format",
        )

        assertEquals(
            "€123.50",
            123.5.formatPrice(
                currencyCode = FALLBACK_CURRENCY,
            ),
            "float price format with one decimal place",
        )

        assertEquals(
            "€123.55",
            123.55.formatPrice(
                currencyCode = FALLBACK_CURRENCY,
            ),
            "float price format with two decimal places",
        )

        assertEquals(
            "€123.56",
            123.555.formatPrice(
                currencyCode = FALLBACK_CURRENCY,
            ),
            "float price format with three decimal places rounding half up",
        )
    }

    @Test
    fun parseNumber() {
        assertEquals(
            123,
            "123".parseNumber()?.toInt(),
            "parse integer",
        )

        assertEquals(
            123456,
            "123,456".parseNumber(
                grouped = true,
            )?.toInt(),
            "parse integer with grouping",
        )

        assertEquals(
            "123.5",
            "123.5".parseNumber()?.format(),
            "parse float",
        )
    }

    @Test
    fun parseDouble() {
        assertEquals(
            123.4,
            "123.4".parseDouble(),
            "parse number as double",
        )
    }

    @Test
    fun parseFloat() {
        assertEquals(
            123.4F,
            "123.4".parseFloat(),
            "parse number as float",
        )
    }

    @Test
    fun parseLong() {
        assertEquals(
            123L,
            "123".parseLong(),
            "parse number as long",
        )

        assertEquals(
            123L,
            "123.4".parseLong(),
            "parse number as long",
        )
    }

    @Test
    fun parseInt() {
        assertEquals(
            123,
            "123".parseInt(),
            "parse number as integer",
        )

        assertEquals(
            123,
            "123.4".parseInt(),
            "parse number as integer",
        )
    }
}
